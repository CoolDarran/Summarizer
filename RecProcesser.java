package com.yeezhao.hwsearch.queryrec.app;


import ciir.umass.edu.learning.DataPoint;
import ciir.umass.edu.learning.RankList;
import ciir.umass.edu.learning.Ranker;
import ciir.umass.edu.learning.RankerFactory;
import ciir.umass.edu.utilities.Sorter;
import com.yeezhao.commons.util.AdvAlgo;
import com.yeezhao.hwsearch.base.HWQuery;
import com.yeezhao.hwsearch.dao.RedisDao;
import com.yeezhao.hwsearch.queryrec.base.CandidateEntry;
import com.yeezhao.hwsearch.queryrec.base.Model;
import com.yeezhao.hwsearch.queryrec.base.QueryRecConsts;
import com.yeezhao.hwsearch.queryrec.serv.QueryRecConf;
import com.yeezhao.hwsearch.queryrec.util.FormatUtil;
import com.yeezhao.hwsearch.queryrec.word.WordEntry;
import com.yeezhao.hwsearch.queryrec.word.WordToVecClient;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by danran on 2014/12/9.
 */
public class RecProcesser {

    private static final String SPACE_STR = " ";
    private static Log LOG = LogFactory.getLog(RecProcesser.class);

    private static final WordToVecClient w2vClient = new WordToVecClient();

    private static RedisDao redisDao;

    private static final Map<String, String> KEY_MAP = new HashMap<String, String>();

    private static Ranker ranker;

    private static int topK = 10;

    static {
        try {
            KEY_MAP.put("session", QueryRecConsts.KEY_QUERY_SESSION);
            KEY_MAP.put("sim-co-keywords", QueryRecConsts.KEY_QUERY_CO_KEYWORDS);
            KEY_MAP.put("sim-tf-iqf", QueryRecConsts.KEY_QUERY_TF_IQF);
            KEY_MAP.put("sim-edit-distance", QueryRecConsts.KEY_QUERY_EDIT_DISTANCE);
            KEY_MAP.put("doc-co-doc", QueryRecConsts.KEY_QUERY_CO_DOCS);
            KEY_MAP.put("doc-product", QueryRecConsts.KEY_QUERY_PRODUCT);
            KEY_MAP.put("doc-doc-keywords", QueryRecConsts.KEY_QUERY_DOC_WORDS);
            KEY_MAP.put("white-list", QueryRecConsts.KEY_WHITE_LIST);
            Configuration conf = new Configuration();
            conf.addResource(QueryRecConsts.FILE_REDIS_CONF);
            redisDao = RedisDao.getInstance(conf);
            RankerFactory rFact = new RankerFactory();
            // TODO replace with true l2r model
            ranker = rFact.loadRanker(QueryRecConf.getInstance().get(QueryRecConsts.PARAM_MODEL_DIR) + QueryRecConf.getInstance().get(QueryRecConsts.PARAM_MODEL_L2R));
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("redis init error");
            System.exit(1);
        }
    }

    public static List<String> genCandidatesFromMemory(HWQuery query) {
        LOG.info("rec for [" + query.getQueryString() + "]");

        if (FormatUtil.blackList(query.getQueryString())) {
            return new ArrayList<String>();
        }

        Map<String, List<CandidateEntry>> queryRecResult = new HashMap<String, List<CandidateEntry>>();
        // 0. white list
        queryRecResult.put("white-list", whiteListRec(query));
        // 1. session
        queryRecResult.put("session", sessionRec(query));
        // 2. sim-co-keywords
        queryRecResult.put("sim-co-keywords", coKeywordsRec(query));
        // 3. sim-tf-iqf
        queryRecResult.put("sim-tf-iqf", tfiqfRec(query));
        // 4. sim-edit-distance
        queryRecResult.put("sim-edit-distance", editdistancRec(query));
        // 5. doc-co-doc
        queryRecResult.put("doc-co-doc", coDocRec(query));
        // 6. doc-product
        queryRecResult.put("doc-product", docProductRec(query));
        // 7. doc-doc-keywords
        queryRecResult.put("doc-doc-keywords", docKeyWords(query));
        // 8. wordvec
        queryRecResult.put("word2vec", queryWord2vec(query));
        // 6. merge all result and return top 10 candidates.
        return mergeAllCandidates(queryRecResult);
    }

    public static List<String> genCandidatesFromRedis(HWQuery query) {
        LOG.info("rec for [" + query.getQueryString() + "]");

        if (FormatUtil.blackList(query.getQueryString())) {
            return new ArrayList<String>();
        }

        Map<String, List<CandidateEntry>> queryRecResult = new HashMap<String, List<CandidateEntry>>();
        // TODO 阈值确认
        // 0. white list
        queryRecResult.put("white-list", recFromRedis("white-list", query, 0.0, topK));
        // 1. session
        queryRecResult.put("session", recFromRedis("session", query, 0.5, topK));
        // 2. sim-co-keywords
        queryRecResult.put("sim-co-keywords", recFromRedis("sim-co-keywords", query, 0.5, topK));
        // 3. sim-tf-iqf
        queryRecResult.put("sim-tf-iqf", recFromRedis("sim-tf-iqf", query, 0.5, topK));
        // 4. sim-edit-distance
        queryRecResult.put("sim-edit-distance", recFromRedis("sim-edit-distance", query, 0.5, topK));
        // 5. doc-co-doc
        queryRecResult.put("doc-co-doc", recFromRedis("doc-co-doc", query, 0.5, topK));
        // 6. doc-product
        queryRecResult.put("doc-product", recFromRedis("doc-product", query, 0.5, topK));
        // 7. doc-doc-keywords
        queryRecResult.put("doc-doc-keywords", recFromRedis("doc-doc-keywords", query, 0.5, topK));
        // 8. wordvec
        queryRecResult.put("word2vec", queryWord2vec(query));

        // 是否生成l2r标注数据
        if (Boolean.parseBoolean(QueryRecConf.getInstance().get("gen.l2r"))) {
            return mergeAllCandiForL2RTagging(query, queryRecResult);
        }
        return mergeAllCandidates(queryRecResult);
    }

    /**
     * 使用阈值进行过滤
     *
     * @param type      model type
     * @param query     query
     * @param threshold filter threshold
     * @param topK
     * @return list of candidates
     */
    private static List<CandidateEntry> recFromRedis(String type, HWQuery query, double threshold, int topK) {
        List<CandidateEntry> candidates = new ArrayList<CandidateEntry>();
        try {
            Map<String, String> all;
            if (query.getResLang() != null && !query.getResLang().equals("")) {
                all = redisDao.hgetAll(query.getResLang() + ":" + KEY_MAP.get(type) + ":" + query.getSearchType() + ":" + query.getQueryString().trim());
            } else {
                all = redisDao.hgetAll(KEY_MAP.get(type) + ":" + query.getQueryString().trim());
            }
            for (Map.Entry<String, String> en : all.entrySet()) {
                // 预处理
                String rec = en.getKey().toLowerCase().trim();
//                rec = FormatUtil.shouldFilter(rec).trim();
                if (Float.parseFloat(en.getValue()) > threshold
                        && !query.getQueryString().toLowerCase().equals(rec)
                        && !query.getQueryString().toLowerCase().contains(rec)
                        && !query.getQueryString().trim().equals(rec)
                        && !query.getQueryString().toLowerCase().replaceAll(" ", "").equals(rec.toLowerCase().replaceAll(" ", ""))
                        && !query.getQueryString().toLowerCase().replaceAll(" ", "").contains(rec.toLowerCase().replaceAll(" ", ""))) {
                    candidates.add(new CandidateEntry(rec, Float.parseFloat(en.getValue())));
                }
            }
            // sort candidate list
            Collections.sort(candidates);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AdvAlgo.findTopK(candidates, topK, new Comparator<CandidateEntry>() {
            @Override
            public int compare(CandidateEntry o1, CandidateEntry o2) {
                if (o1.score > o2.score)
                    return 1;
                else if (o1.score < o2.score)
                    return -1;
                else
                    return 0;
            }
        });
    }

    private static List<String> mergeAllCandiForL2RTagging(HWQuery query, Map<String, List<CandidateEntry>> queryRecCandidates) {
        int modelCount = 0;

        List<CandidateEntry> sessionCandidates = queryRecCandidates.get("session");
        LOG.info("candidates from session: " + sessionCandidates.size());
        modelCount += (sessionCandidates.size() != 0 ? 1 : 0);

        List<CandidateEntry> simCoKeywordsCandidates = queryRecCandidates.get("sim-co-keywords");
        LOG.info("candidates from sim-co-keywords: " + simCoKeywordsCandidates.size());
        modelCount += (simCoKeywordsCandidates.size() != 0 ? 1 : 0);

        List<CandidateEntry> simTfIqfCandidates = queryRecCandidates.get("sim-tf-iqf");
        LOG.info("candidates from sim-tf-iqf: " + simTfIqfCandidates.size());
        modelCount += (simTfIqfCandidates.size() != 0 ? 1 : 0);

        List<CandidateEntry> simEdiDistCandidates = queryRecCandidates.get("sim-edit-distance");
        LOG.info("candidates from sim-edit-distance: " + simEdiDistCandidates.size());
        modelCount += (simEdiDistCandidates.size() != 0 ? 1 : 0);

        List<CandidateEntry> docCoDocsCandidates = queryRecCandidates.get("doc-co-doc");
        LOG.info("candidates from doc-co-doc: " + docCoDocsCandidates.size());
        modelCount += (docCoDocsCandidates.size() != 0 ? 1 : 0);

        List<CandidateEntry> docProductCandidates = queryRecCandidates.get("doc-product");
        LOG.info("candidates from doc-product: " + docProductCandidates.size());
        modelCount += (docProductCandidates.size() != 0 ? 1 : 0);

        List<CandidateEntry> docKeyWordsCandidates = queryRecCandidates.get("doc-doc-keywords");
        LOG.info("candidates from doc-doc-keywords: " + docKeyWordsCandidates.size());
        modelCount += (docKeyWordsCandidates.size() != 0 ? 1 : 0);

        List<CandidateEntry> word2vecCandidates = queryRecCandidates.get("word2vec");
        LOG.info("candidates from word2vec: " + word2vecCandidates.size());
        modelCount += (word2vecCandidates.size() != 0 ? 1 : 0);
        List<String> result = new ArrayList<String>();
        if (modelCount != 0) {
            int avgSize = 10 / modelCount;
            int left = 10 % modelCount;
            Map<String, float[]> vectors = new HashMap<String, float[]>();

            // add session candidates
            int count = 0;
            for (CandidateEntry en : sessionCandidates) {
                if (count < avgSize + left) {
                    addCandidatesToVector(vectors, en, 0);
                    count++;
                }
            }
            count = 0;

            // add sim-co-keywords candidates
            for (CandidateEntry en : simCoKeywordsCandidates) {
                if (count < avgSize + left) {
                    addCandidatesToVector(vectors, en, 1);
                    count++;
                }
            }
            count = 0;

            // add sim-tf-iqf candidates
            for (CandidateEntry en : simTfIqfCandidates) {
                if (count < avgSize + left) {
                    addCandidatesToVector(vectors, en, 2);
                    count++;
                }
            }
            count = 0;

            // add sim-edit-distance candidates
            for (CandidateEntry en : simEdiDistCandidates) {
                if (count < avgSize + left) {
                    addCandidatesToVector(vectors, en, 3);
                    count++;
                }
            }
            count = 0;

            // add doc-co-doc candidates
            for (CandidateEntry en : docCoDocsCandidates) {
                if (count < avgSize + left) {
                    addCandidatesToVector(vectors, en, 4);
                    count++;
                }
            }
            count = 0;

            // add doc-product candidates
            for (CandidateEntry en : docProductCandidates) {
                if (count < avgSize + left) {
                    addCandidatesToVector(vectors, en, 5);
                    count++;
                }
            }
            count = 0;

            // add doc-doc-keywords candidates
            for (CandidateEntry en : docKeyWordsCandidates) {
                if (count < avgSize + left) {
                    addCandidatesToVector(vectors, en, 6);
                    count++;
                }
            }
            count = 0;

            // add word2vec candidates
            for (CandidateEntry en : word2vecCandidates) {
                if (count < avgSize + left) {
                    addCandidatesToVector(vectors, en, 7);
                    count++;
                }
            }

            for (Map.Entry<String, float[]> vector : vectors.entrySet()) {
                result.add(vector.getKey());
            }
            genCSVFileForL2R(query, vectors);
        }
        return result;
    }

    private static void addCandidatesToVector(Map<String, float[]> vectors, CandidateEntry en, int type) {
        String rec = en.name.toLowerCase().trim();
        rec = FormatUtil.shouldFilter(rec).trim();
        if (vectors.containsKey(rec)) {
            float[] vector = vectors.get(rec);
            vector[type] = en.score;
            vectors.put(rec, vector);
        } else {
            float[] vector = new float[8];
            vector[type] = en.score;
            vectors.put(rec, vector);
        }
    }

    /**
     * 生成L2R标注文件
     *
     * @param q
     * @param vectors
     */
    private static void genCSVFileForL2R(HWQuery q, Map<String, float[]> vectors) {
        FileWriter fileWriter = null;
        CSVPrinter csvFilePrinter = null;

        //Create the CSVFormat object with "\n" as a record delimiter
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
        String fileName = "queryRec-label.csv";
//        String[] header = new String[]{"输入Query", "推荐Query",	"相关度得分（0-4）",	"Vector"};
        try {
            //initialize FileWriter object
            fileWriter = new FileWriter(fileName, true);

            //initialize CSVPrinter object
            csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

            //Create CSV file header
//            csvFilePrinter.printRecord(header);

            //Write record list to the CSV file
            boolean isFirst = true;
            for (Map.Entry<String, float[]> vector : vectors.entrySet()) {
                String query = q.getQueryString();
                if (!isFirst) {
                    query = "";
                }
                StringBuilder sb = new StringBuilder();
                for (float score : vector.getValue()) {
                    sb.append(score).append(",");
                }
                List<String> record = new ArrayList<String>(4);
                record.add(query);
                record.add(vector.getKey());
                record.add("");
                record.add(sb.toString().substring(0, sb.toString().length() - 1));
                csvFilePrinter.printRecord(record);
                isFirst = false;
            }
            csvFilePrinter.println();
        } catch (IOException e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.flush();
                }
                if (fileWriter != null) {
                    fileWriter.close();
                }
                if (csvFilePrinter != null) {
                    csvFilePrinter.close();
                }
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter/csvPrinter !!!");
                e.printStackTrace();
            }
        }
    }

    /**
     * TODO 整合所有模型结果
     * 1. 简单weight
     * 2. learn to rank
     *
     * @param queryRecCandidates all candidates
     * @return merged candidates
     */
    private static List<String> mergeAllCandidates(Map<String, List<CandidateEntry>> queryRecCandidates) {
        List<String> result = new ArrayList<String>();

        Set<String> alreadyCan = new HashSet<String>();

        /** 白名单 */
        List<CandidateEntry> whiteList = queryRecCandidates.remove("white-list");
        for (CandidateEntry candidateEntry : whiteList) {
            String word = candidateEntry.name;
            if (!alreadyCan.contains(word.replaceAll(" ", ""))) {
                if (result.size() < topK) {
                    result.add(word);
                    alreadyCan.add(word.replaceAll(" ", ""));
                    LOG.info(String.format("%s : [%s]", word, "from white list"));
                }
            }
        }

        LinkedHashMap<String, float[]> features = genFetureString(queryRecCandidates);
        // l2r
        RankList rankList = candidates2RankList(features); // a ranklist contain several datapoint(each represents a candidate)
        double[] scores = new double[rankList.size()];
        for (int j = 0; j < rankList.size(); j++)
            scores[j] = ranker.eval(rankList.get(j));
        int[] idx = Sorter.sort(scores, false);
        String[] candidatesIndex = features.keySet().toArray(new String[features.keySet().size()]);
        // return topK suggestion
        for (int j = 0; j < rankList.size() && result.size() < topK; j++) {
            int index = idx[j];
            String word = candidatesIndex[index];
            if (!alreadyCan.contains(word.replaceAll(" ", ""))) {
                result.add(word);
                alreadyCan.add(word.replaceAll(" ", ""));
                StringBuilder sb = new StringBuilder();
                for (float a : features.get(word)) {
                    sb.append(a).append(",");
                }
                LOG.info(String.format("%s : [%s]", word, sb.toString().substring(0, sb.toString().length() - 1)));
            }
        }
        return result;
    }

    private static LinkedHashMap<String, float[]> genFetureString(Map<String, List<CandidateEntry>> queryRecCandidates) {
        List<CandidateEntry> sessionCandidates = queryRecCandidates.get("session");
        LOG.info("candidates from session: " + sessionCandidates.size());

        List<CandidateEntry> simCoKeywordsCandidates = queryRecCandidates.get("sim-co-keywords");
        LOG.info("candidates from sim-co-keywords: " + simCoKeywordsCandidates.size());

        List<CandidateEntry> simTfIqfCandidates = queryRecCandidates.get("sim-tf-iqf");
        LOG.info("candidates from sim-tf-iqf: " + simTfIqfCandidates.size());

        List<CandidateEntry> simEdiDistCandidates = queryRecCandidates.get("sim-edit-distance");
        LOG.info("candidates from sim-edit-distance: " + simEdiDistCandidates.size());

        List<CandidateEntry> docCoDocsCandidates = queryRecCandidates.get("doc-co-doc");
        LOG.info("candidates from doc-co-doc: " + docCoDocsCandidates.size());

        List<CandidateEntry> docProductCandidates = queryRecCandidates.get("doc-product");
        LOG.info("candidates from doc-product: " + docProductCandidates.size());

        List<CandidateEntry> docKeyWordsCandidates = queryRecCandidates.get("doc-doc-keywords");
        LOG.info("candidates from doc-doc-keywords: " + docKeyWordsCandidates.size());

        List<CandidateEntry> word2vecCandidates = queryRecCandidates.get("word2vec");
        LOG.info("candidates from word2vec: " + word2vecCandidates.size());

        Map<String, float[]> features = new HashMap<String, float[]>();

        // add session candidates
        int count = 0;
        for (CandidateEntry en : sessionCandidates) {
            addCandidatesToVector(features, en, 0);
            count++;
        }

        // add sim-co-keywords candidates
        for (CandidateEntry en : simCoKeywordsCandidates) {
            addCandidatesToVector(features, en, 1);
            count++;
        }

        // add sim-tf-iqf candidates
        for (CandidateEntry en : simTfIqfCandidates) {
            addCandidatesToVector(features, en, 2);
            count++;
        }

        // add sim-edit-distance candidates
        for (CandidateEntry en : simEdiDistCandidates) {
            addCandidatesToVector(features, en, 3);
            count++;
        }

        // add doc-co-doc candidates
        for (CandidateEntry en : docCoDocsCandidates) {
            addCandidatesToVector(features, en, 4);
            count++;
        }

        // add doc-product candidates
        for (CandidateEntry en : docProductCandidates) {
            addCandidatesToVector(features, en, 5);
            count++;
        }

        // add doc-doc-keywords candidates
        for (CandidateEntry en : docKeyWordsCandidates) {
            addCandidatesToVector(features, en, 6);
            count++;
        }

        // add word2vec candidates
        for (CandidateEntry en : word2vecCandidates) {
            addCandidatesToVector(features, en, 7);
            count++;
        }

        LOG.info("candidate size: " + features.size());

        return new LinkedHashMap<String, float[]>(features);
    }

    private static RankList candidates2RankList(Map<String, float[]> features) {
        RankList rankList = new RankList();
        for (Map.Entry<String, float[]> feature : features.entrySet()) {
            DataPoint dp = new DataPoint(floatVec2Str(feature));
            rankList.add(dp);
        }
        return rankList;
    }

    private static String floatVec2Str(Map.Entry<String, float[]> feature) {
        StringBuilder sb = new StringBuilder();
        sb.append(0);
        sb.append(SPACE_STR).append("qid:").append(feature.getKey().replace(" ", "_"));
        int len = feature.getValue().length;
        for (int i = 1; i < len; i++) {
            sb.append(SPACE_STR).append(i).append(":").append(feature.getValue()[i]);
        }
        sb.append(" # ").append(feature.getKey());
        return sb.toString();
    }


    /**
     * 获取word2vec推荐结果
     *
     * @param query query
     * @return candidates
     */
    private static List<CandidateEntry> queryWord2vec(HWQuery query) {
        List<CandidateEntry> result = new ArrayList<CandidateEntry>();
        String queryStr = query.getQueryString();
        List<Term> ts = NlpAnalysis.parse(queryStr);
        List<String> terms = new ArrayList<String>();
        for (Term t : ts) {
            terms.add(t.getName());
        }

        // 1. all to word2vec
        List<WordEntry> words = new ArrayList<WordEntry>();
        try {
            words = w2vClient.getRelevantWords(queryStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<CandidateEntry> oneTerms = new ArrayList<CandidateEntry>();
        for (WordEntry en : words) {
            if (!queryStr.contains(en.name)) {
                oneTerms.add(new CandidateEntry(en.name, en.score));
            }
        }
        result.addAll(oneTerms);

        // 2. one to many association
        if (oneTerms.size() == 0) {
            List<CandidateEntry> expendTerms = new ArrayList<CandidateEntry>();
            for (String term : terms) {
                List<WordEntry> relevantWords;
                try {
                    if (!FormatUtil.blackList(term)) {
                        relevantWords = w2vClient.getRelevantWords(term);
                        for (WordEntry word : relevantWords) {
                            if (!queryStr.contains(word.name)) {
                                float score = w2vClient.wordsToWordDistance(terms.toArray(new String[terms.size()]), word.name);
                                expendTerms.add(new CandidateEntry(queryStr + " " + word.name, score));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            result.addAll(expendTerms);
        }

        return AdvAlgo.findTopK(result, 10, new Comparator<CandidateEntry>() {
            @Override
            public int compare(CandidateEntry o1, CandidateEntry o2) {
                if (o1.score > o2.score)
                    return 1;
                return 0;
            }
        });
    }

    /**
     * 编辑距离
     *
     * @param query query
     * @return candidates
     */
    private static List<CandidateEntry> editdistancRec(HWQuery query) {
        return (Model.SIM_EDIT_DISTANCE.get(query) != null) ? Model.SIM_EDIT_DISTANCE.get(query) : new ArrayList<CandidateEntry>();
    }

    /**
     * 共现关键词
     *
     * @param query query
     * @return candidates
     */
    private static List<CandidateEntry> coKeywordsRec(HWQuery query) {
        return (Model.SIM_CO_KEYWORDS_REC.get(query) != null) ? Model.SIM_CO_KEYWORDS_REC.get(query) : new ArrayList<CandidateEntry>();
    }

    /**
     * tfiqf
     *
     * @param query query
     * @return candidates
     */
    private static List<CandidateEntry> tfiqfRec(HWQuery query) {
        return (Model.SIM_TF_IQF_REC.get(query) != null) ? Model.SIM_TF_IQF_REC.get(query) : new ArrayList<CandidateEntry>();
    }

    /**
     * 共现点击文档
     *
     * @param query query
     * @return candidates
     */
    private static List<CandidateEntry> coDocRec(HWQuery query) {
        return (Model.DOC_CO_CLICKED_DOCS.get(query) != null) ? Model.DOC_CO_CLICKED_DOCS.get(query) : new ArrayList<CandidateEntry>();
    }

    /**
     * 产品结构
     *
     * @param query query
     * @return candidates
     */
    private static List<CandidateEntry> docProductRec(HWQuery query) {
        return (Model.DOC_HIERARCHY_PRODUCT.get(query) != null) ? Model.DOC_HIERARCHY_PRODUCT.get(query) : new ArrayList<CandidateEntry>();
    }

    /**
     * 产品结构
     *
     * @param query query
     * @return candidates
     */
    private static List<CandidateEntry> docKeyWords(HWQuery query) {
        return (Model.DOC_CLICKED_DOCS_WORDS.get(query) != null) ? Model.DOC_CLICKED_DOCS_WORDS.get(query) : new ArrayList<CandidateEntry>();
    }

    /**
     * 获取相关结果，使用Session
     *
     * @param query query
     * @return candidates
     */
    private static List<CandidateEntry> sessionRec(HWQuery query) {
        return (Model.SESSION_REC.get(query) != null) ? Model.SESSION_REC.get(query) : new ArrayList<CandidateEntry>();
    }

    /**
     * 获取白名单
     * @param query
     * @return
     */
    private static List<CandidateEntry> whiteListRec(HWQuery query) {
        return new ArrayList<CandidateEntry>();
    }
}

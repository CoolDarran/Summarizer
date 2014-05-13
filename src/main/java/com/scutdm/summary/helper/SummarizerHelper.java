package com.scutdm.summary.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.ictclas4j.bean.SegResult;
import org.ictclas4j.segment.SegTag;

import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.scutdm.summary.analyz.CVectorBuilder;
import com.scutdm.summary.analyz.EVectorBuilder;
import com.scutdm.summary.cluster.CSentenceDBSCAN;
import com.scutdm.summary.cluster.CWordDBSCAN;
import com.scutdm.summary.cluster.ESentenceDBSCAN;
import com.scutdm.summary.cluster.EWordDBSCAN;
import com.scutdm.summary.doc.EArticle;
import com.scutdm.summary.doc.CArticle;
import com.scutdm.summary.doc.CSentence;
import com.scutdm.summary.doc.CWord;
import com.scutdm.summary.doc.ESentence;
import com.scutdm.summary.doc.EWord;
import com.scutdm.summary.preprocess.CUtility;
import com.scutdm.summary.preprocess.Utility;
import com.scutdm.summary.summary.CSubTopic;
import com.scutdm.summary.summary.ESubTopic;
import com.scutdm.summary.utility.CommonUtility;

import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.POS;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.sussex.nlp.jws.JWS;

/**
 * summarizer helper 
 * using to pass text to summarizer
 * 
 * @author L.danran
 * Created on Apr 5, 2014
 */
public class SummarizerHelper {

	/**
	 * pass text to summarizer
	 * 
	 * @param chinese
	 * @param keyWords 
	 * @param textList
	 * @param ws 
	 * @param dict 
	 */
	public String passText(boolean chinese, String keyWords, List<String> textList, JWS ws, IDictionary dict) {
		String summaryContent = "";
		if(chinese){
			System.setProperty("file.encoding","gb2312");
			// for chinese text
			summaryContent = chinSummary(textList, keyWords);
		}else{
			// for english text
			summaryContent = engSummary(textList, keyWords, ws,dict);
		}
		return summaryContent;
	}

	private String engSummary(List<String> textList, String keyWords, JWS ws, IDictionary dict) {
		
		TokenizerFactory TOKENIZER_FACTORY = new IndoEuropeanTokenizerFactory();
	    SentenceModel SENTENCE_MODEL  = new MedlineSentenceModel();
	    MaxentTagger tagger = new MaxentTagger("taggers/english-left3words-distsim.tagger");
		
		EArticle[] articles = new EArticle[textList.size()];
		
		long startTime = System.currentTimeMillis();
		for(int i = 0; i < articles.length; i++){
			articles[i] = new EArticle(String.valueOf(i), textList.get(i), TOKENIZER_FACTORY,SENTENCE_MODEL,tagger);
		}
		long endTime = System.currentTimeMillis();
		CommonUtility.printTimeElapsed("Article Construct", startTime, endTime);
		
		startTime = System.currentTimeMillis();
		List<EWord> nounWords = new ArrayList<EWord>();
		List<EWord> verbWords = new ArrayList<EWord>();
		for(EArticle article:articles){
			if (article.getArticleString() != null) {
				List<EWord> tempNounWords = new ArrayList<EWord>();
				List<EWord> tempVerbWords = new ArrayList<EWord>();
				for (ESentence sentence : article.getSentences()) {
//					System.out.println("\nNoun:");
					for (EWord word : sentence.getNounWords()) {
//						System.out.print(word.getLemma() + " ");
						tempNounWords.add(word);
						nounWords.add(word);
					}
//					System.out.println("\nVerb:");
					for (EWord word : sentence.getVerbWords()) {
//						System.out.print(word.getLemma() + " ");
						tempVerbWords.add(word);
						verbWords.add(word);
					}
				}
				EWord[] nouns = new EWord[tempNounWords.size()];
				Utility.senseDisambiguation(tempNounWords.toArray(nouns), POS.NOUN,ws,dict);
				EWord[] verbs = new EWord[tempVerbWords.size()];
				Utility.senseDisambiguation(tempVerbWords.toArray(verbs), POS.VERB,ws,dict);
			}
		}
		endTime = System.currentTimeMillis();
		CommonUtility.printTimeElapsed("Words Sense Disambiguation", startTime, endTime);
		
		startTime = System.currentTimeMillis();
		//根据语义对词语聚类
		EWordDBSCAN dbscanNoun = new EWordDBSCAN();
		dbscanNoun.Cluster(nounWords,0.6,3,"n",ws);
		EWordDBSCAN dbscanVerb = new EWordDBSCAN();
		dbscanVerb.Cluster(verbWords,0.6,3,"v",ws);
		endTime = System.currentTimeMillis();
		CommonUtility.printTimeElapsed("Words Cluster", startTime, endTime);
		
		EVectorBuilder vectorBuilder = new EVectorBuilder();
		List<ESentence> sentenceList = new LinkedList<ESentence>();
		for(EArticle article:articles){
			if(article.getSentences() != null){
				for(ESentence sentence:article.getSentences()){
					vectorBuilder.sentVecBuilder(sentence);
					sentenceList.add(sentence);
				}	
			}
		}
		
		startTime = System.currentTimeMillis();
		//句子聚类分析
		List<ESubTopic> subTopics = new LinkedList<ESubTopic>();
		ESentenceDBSCAN sentenceDBSCAN = new ESentenceDBSCAN();
		sentenceDBSCAN.Cluster(subTopics,sentenceList, 0.6, 3);
		endTime = System.currentTimeMillis();
		CommonUtility.printTimeElapsed("Sentences Cluster", startTime, endTime);
		
		//对子主题排序
		Collections.sort(subTopics,new Comparator<ESubTopic>(){  
            public int compare(ESubTopic arg0, ESubTopic arg1) {  
                return arg1.getScore().compareTo(arg0.getScore());  
            }  
        });
		
		String summary = "";
		for(ESubTopic subTopic:subTopics){
//			System.out.println(subTopic.getSummarySent());
			summary += subTopic.getSummarySent() + "\n";
		}

		return summary;
	}

	private static String chinSummary(List<String> textList, String keyWords) {
		
		CArticle[] articles = new CArticle[textList.size()];
		for(int i = 0; i < articles.length; i++){
			articles[i] = new CArticle(String.valueOf(i), textList.get(i));
		}
		
		List<CWord> nounWords = new ArrayList<CWord>();
		List<CWord> verbWords = new ArrayList<CWord>();
		for(CArticle article:articles){
			List<CWord> tempNounWords = new ArrayList<CWord>();
			List<CWord> tempVerbWords = new ArrayList<CWord>();
			for(CSentence sentence:article.getSentences()){
//				System.out.println("\nNoun:");
				for(CWord word:sentence.getNounWords()){
//					System.out.print(word.getLemma() + " ");
					tempNounWords.add(word);
					nounWords.add(word);
				}
//				System.out.println("\nVerb:");
				for(CWord word:sentence.getVerbWords()){
//					System.out.print(word.getLemma() + " ");
					tempVerbWords.add(word);
					verbWords.add(word);
				}
			}
			CWord[] nouns = new CWord[tempNounWords.size()];
			nouns = tempNounWords.toArray(nouns);
			CUtility.senseDisambiguation(nouns);

			CWord[] verbs = new CWord[tempVerbWords.size()];
			verbs = tempVerbWords.toArray(verbs);
			CUtility.senseDisambiguation(verbs);
		}
		
		CWordDBSCAN dbscanNoun = new CWordDBSCAN();
		dbscanNoun.Cluster(nounWords,0.95,3,"n");
		CWordDBSCAN dbscanVerb = new CWordDBSCAN();
		dbscanVerb.Cluster(verbWords,0.9,3,"v");
		
		CVectorBuilder vectorBuilder = new CVectorBuilder();
		List<CSentence> sentenceList = new LinkedList<CSentence>();
		for(CArticle article:articles){
			for(CSentence sentence:article.getSentences()){
				vectorBuilder.sentVecBuilder(sentence);
				sentenceList.add(sentence);
			}			
		}
		
		List<CSubTopic> subTopics = new LinkedList<CSubTopic>();
		CSentenceDBSCAN sentenceDBSCAN = new CSentenceDBSCAN();
		sentenceDBSCAN.Cluster(subTopics,sentenceList, 0.8, 3);
		
		Collections.sort(subTopics,new Comparator<CSubTopic>(){  
            public int compare(CSubTopic arg0, CSubTopic arg1) {  
                return arg1.getScore().compareTo(arg0.getScore());  
            }  
        });
		
		String summary = "";
		for(CSubTopic subTopic:subTopics){
			summary += subTopic.getSummarySent() + "\n";
		}

		return summary;
	}
}

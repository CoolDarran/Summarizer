package com.scutdm.summary.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.wrap.chinsummarizer.analyz.VectorBuilder;
import com.wrap.chinsummarizer.cluster.SentenceDBSCAN;
import com.wrap.chinsummarizer.cluster.WordDBSCAN;
import com.wrap.chinsummarizer.doc.Article;
import com.wrap.chinsummarizer.doc.Sentence;
import com.wrap.chinsummarizer.doc.Word;
import com.wrap.chinsummarizer.preprocess.Utility;
import com.wrap.chinsummarizer.summary.SubTopic;

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
		
		com.wrap.engsummarizer.doc.Article[] articles = new com.wrap.engsummarizer.doc.Article[textList.size()];
		
		long startTime = System.currentTimeMillis();
		for(int i = 0; i < articles.length; i++){
			articles[i] = new com.wrap.engsummarizer.doc.Article(String.valueOf(i), textList.get(i), TOKENIZER_FACTORY,SENTENCE_MODEL,tagger);
		}
		long endTime = System.currentTimeMillis();
		com.scutdm.summary.utility.Utility.printTimeElapsed("Article Construct", startTime, endTime);
		
		startTime = System.currentTimeMillis();
		List<com.wrap.engsummarizer.doc.Word> nounWords = new ArrayList<com.wrap.engsummarizer.doc.Word>();
		List<com.wrap.engsummarizer.doc.Word> verbWords = new ArrayList<com.wrap.engsummarizer.doc.Word>();
		for(com.wrap.engsummarizer.doc.Article article:articles){
			if (article.getArticleString() != null) {
				List<com.wrap.engsummarizer.doc.Word> tempNounWords = new ArrayList<com.wrap.engsummarizer.doc.Word>();
				List<com.wrap.engsummarizer.doc.Word> tempVerbWords = new ArrayList<com.wrap.engsummarizer.doc.Word>();
				for (com.wrap.engsummarizer.doc.Sentence sentence : article.getSentences()) {
//					System.out.println("\nNoun:");
					for (com.wrap.engsummarizer.doc.Word word : sentence.getNounWords()) {
//						System.out.print(word.getLemma() + " ");
						tempNounWords.add(word);
						nounWords.add(word);
					}
//					System.out.println("\nVerb:");
					for (com.wrap.engsummarizer.doc.Word word : sentence.getVerbWords()) {
//						System.out.print(word.getLemma() + " ");
						tempVerbWords.add(word);
						verbWords.add(word);
					}
				}
				com.wrap.engsummarizer.doc.Word[] nouns = new com.wrap.engsummarizer.doc.Word[tempNounWords.size()];
				com.wrap.engsummarizer.preprocess.Utility.senseDisambiguation(tempNounWords.toArray(nouns), POS.NOUN,ws,dict);
				com.wrap.engsummarizer.doc.Word[] verbs = new com.wrap.engsummarizer.doc.Word[tempVerbWords.size()];
				com.wrap.engsummarizer.preprocess.Utility.senseDisambiguation(tempVerbWords.toArray(verbs), POS.VERB,ws,dict);
			}
		}
		endTime = System.currentTimeMillis();
		com.scutdm.summary.utility.Utility.printTimeElapsed("Words Sense Disambiguation", startTime, endTime);
		
		startTime = System.currentTimeMillis();
		//根据语义对词语聚类
		com.wrap.engsummarizer.cluster.WordDBSCAN dbscanNoun = new com.wrap.engsummarizer.cluster.WordDBSCAN();
		dbscanNoun.Cluster(nounWords,0.6,3,"n",ws);
		com.wrap.engsummarizer.cluster.WordDBSCAN dbscanVerb = new com.wrap.engsummarizer.cluster.WordDBSCAN();
		dbscanVerb.Cluster(verbWords,0.6,3,"v",ws);
		endTime = System.currentTimeMillis();
		com.scutdm.summary.utility.Utility.printTimeElapsed("Words Cluster", startTime, endTime);
		
		com.wrap.engsummarizer.analyz.VectorBuilder vectorBuilder = new com.wrap.engsummarizer.analyz.VectorBuilder();
		List<com.wrap.engsummarizer.doc.Sentence> sentenceList = new LinkedList<com.wrap.engsummarizer.doc.Sentence>();
		for(com.wrap.engsummarizer.doc.Article article:articles){
			if(article.getSentences() != null){
				for(com.wrap.engsummarizer.doc.Sentence sentence:article.getSentences()){
					vectorBuilder.sentVecBuilder(sentence);
					sentenceList.add(sentence);
				}	
			}
		}
		
		startTime = System.currentTimeMillis();
		//句子聚类分析
		List<com.wrap.engsummarizer.summary.SubTopic> subTopics = new LinkedList<com.wrap.engsummarizer.summary.SubTopic>();
		com.wrap.engsummarizer.cluster.SentenceDBSCAN sentenceDBSCAN = new com.wrap.engsummarizer.cluster.SentenceDBSCAN();
		sentenceDBSCAN.Cluster(subTopics,sentenceList, 0.6, 3);
		endTime = System.currentTimeMillis();
		com.scutdm.summary.utility.Utility.printTimeElapsed("Sentences Cluster", startTime, endTime);
		
		//对子主题排序
		Collections.sort(subTopics,new Comparator<com.wrap.engsummarizer.summary.SubTopic>(){  
            public int compare(com.wrap.engsummarizer.summary.SubTopic arg0, com.wrap.engsummarizer.summary.SubTopic arg1) {  
                return arg1.getScore().compareTo(arg0.getScore());  
            }  
        });
		
		String summary = "";
		for(com.wrap.engsummarizer.summary.SubTopic subTopic:subTopics){
//			System.out.println(subTopic.getSummarySent());
			summary += subTopic.getSummarySent() + "\n";
		}

		return summary;
	}

	private static String chinSummary(List<String> textList, String keyWords) {
		
		Article[] articles = new Article[textList.size()];
		for(int i = 0; i < articles.length; i++){
			articles[i] = new Article(String.valueOf(i), textList.get(i));
		}
		
		List<Word> nounWords = new ArrayList<Word>();
		List<Word> verbWords = new ArrayList<Word>();
		for(Article article:articles){
			List<Word> tempNounWords = new ArrayList<Word>();
			List<Word> tempVerbWords = new ArrayList<Word>();
			for(Sentence sentence:article.getSentences()){
//				System.out.println("\nNoun:");
				for(Word word:sentence.getNounWords()){
//					System.out.print(word.getLemma() + " ");
					tempNounWords.add(word);
					nounWords.add(word);
				}
//				System.out.println("\nVerb:");
				for(Word word:sentence.getVerbWords()){
//					System.out.print(word.getLemma() + " ");
					tempVerbWords.add(word);
					verbWords.add(word);
				}
//				System.out.println();
			}
			Word[] nouns = new Word[tempNounWords.size()];
			nouns = tempNounWords.toArray(nouns);
			Utility.senseDisambiguation(nouns);

			Word[] verbs = new Word[tempVerbWords.size()];
			verbs = tempVerbWords.toArray(verbs);
			Utility.senseDisambiguation(verbs);
		}
		
		WordDBSCAN dbscanNoun = new WordDBSCAN();
		dbscanNoun.Cluster(nounWords,0.95,3,"n");
		WordDBSCAN dbscanVerb = new WordDBSCAN();
		dbscanVerb.Cluster(verbWords,0.9,3,"v");
		
		VectorBuilder vectorBuilder = new VectorBuilder();
		List<Sentence> sentenceList = new LinkedList<Sentence>();
		for(Article article:articles){
			for(Sentence sentence:article.getSentences()){
				vectorBuilder.sentVecBuilder(sentence);
				sentenceList.add(sentence);
			}			
		}
		
		List<SubTopic> subTopics = new LinkedList<SubTopic>();
		SentenceDBSCAN sentenceDBSCAN = new SentenceDBSCAN();
		sentenceDBSCAN.Cluster(subTopics,sentenceList, 0.8, 3);
		
		Collections.sort(subTopics,new Comparator<SubTopic>(){  
            public int compare(SubTopic arg0, SubTopic arg1) {  
                return arg1.getScore().compareTo(arg0.getScore());  
            }  
        });
		
		String summary = "";
		for(SubTopic subTopic:subTopics){
			summary += subTopic.getSummarySent() + "\n";
		}

		return summary;
	}

}

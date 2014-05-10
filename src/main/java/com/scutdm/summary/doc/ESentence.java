package com.scutdm.summary.doc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.scutdm.summary.preprocess.Utility;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * 英文句子类
 * @author wRap
 *
 */

public class ESentence{
	private EWord[] words;	//词语对象
	private int length;
	private String sentenceString;	//原句子串
	public Map<String,Integer> concept;	//语义概念向量
	private String[] wordClusterStrings;	//语义概念序列
	private boolean isKey;
	private boolean isClassed;
	private String clusterID;	//句子聚类结果的簇标号
	private double score;		//句子得分
	private String articleID;	//所属文章号
	private String position;	//在文章中所处位置
	
	/**
	 * added by Darran.L at 2014.5.7
	 * to use in word construct
	 */
	private static AtomicLong storeID = new AtomicLong();
	
	public ESentence(String articleID,String position,MaxentTagger tagger,String sentence){
		String tagged = Utility.wordTag(tagger,sentence);	//词性标注
		List<EWord> wordList = new LinkedList<EWord>();
		String[] words = tagged.split(" ");
	    for(String word:words){
	    	String lemma = word.split("_")[0];
	    	String partOfSpeech = word.split("_")[1];
	    	EWord wordObj = new EWord(lemma, partOfSpeech, storeID.getAndDecrement());
//	    	Word wordObj = new Word(lemma, partOfSpeech);	    	
	    	wordList.add(wordObj);
	    }
	    EWord[] wordArray = new EWord[wordList.size()];
		setSentenceString(sentence);
	    setWords(wordList.toArray(wordArray));
	    setClusterID("");
	    setScore(0);
	    setArticleID(articleID);
	    setPosition(position);
	    concept = new HashMap<String, Integer>();
	}
	public EWord[] getWords() {
		return words;
	}
	public void setWords(EWord[] words) {
		this.words = words;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public String getSentenceString() {
		return sentenceString;
	}
	public void setSentenceString(String sentenceString) {
		this.sentenceString = sentenceString;
	}
	public String[] getWordClusterStrings() {
		return wordClusterStrings;
	}
	public void setWordClusterStrings(String[] wordClusterStrings) {
		this.wordClusterStrings = wordClusterStrings;
	}
	public boolean isKey() {
		return isKey;
	}
	public void setKey(boolean isKey) {
		this.isKey = isKey;
	}
	public boolean isClassed() {
		return isClassed;
	}
	public void setClassed(boolean isClassed) {
		this.isClassed = isClassed;
	}
	public String getClusterID() {
		return clusterID;
	}
	public void setClusterID(String clusterID) {
		this.clusterID = clusterID;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public String getArticleID() {
		return articleID;
	}
	public void setArticleID(String articleID) {
		this.articleID = articleID;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public void printWords(){
//		for(Word word:words){
//			System.out.print(word.getLemma()+"|"+word.getPOS()+" ");
//		}
//		System.out.println();
	}
	public List<EWord> getNounWords(){
		List<EWord> wordList = new LinkedList<EWord>();
		for(EWord word:words){
			if(word.getPOS().startsWith("N")){
				wordList.add(word);
			}
		}
		return wordList;
	}
	public List<EWord> getVerbWords(){
		List<EWord> wordList = new LinkedList<EWord>();
		for(EWord word:words){
			if(word.getPOS().startsWith("V")){
				wordList.add(word);
			}
		}
		return wordList;
	}
	
}

package com.scutdm.summary.doc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.scutdm.summary.preprocess.CUtility;

/**
 * 中文句子类
 * @author wRap
 *
 */
public class CSentence{
	private CWord[] words;	//词语对象
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
	
	public CSentence(String articleID,String position,String sentence){
		if (sentence!=null) {
			sentence = sentence.trim();
			Pattern p = Pattern.compile(" |\\s+|\t|\r|\n");
			Matcher m = p.matcher(sentence);
			sentence = m.replaceAll("，");
			m = p.matcher(sentence);
			sentence = m.replaceAll("");
		}
		
		// 分词
		String tagged = CUtility.ICTCLAS(sentence);
		String[] words = tagged.split(" ");
		
//		LinkedList<String> words = CUtility.IKAnalysis(sentence);
		
		List<CWord> wordList = new LinkedList<CWord>();
	    for(String word:words){
	    	if(word.split("/").length<2)
	    		continue;
	    	String lemma = word.split("/")[0];
	    	String partOfSpeech = word.split("/")[1];
	    	
	    	CWord wordObj = new CWord(lemma, partOfSpeech);
	    	wordList.add(wordObj);
	    }
	    CWord[] wordArray = new CWord[wordList.size()];
		setSentenceString(sentence);
	    setWords(wordList.toArray(wordArray));
	    setClusterID("");
	    setScore(0);
	    setArticleID(articleID);
	    setPosition(position);
	    concept = new HashMap<String, Integer>();
	}
	public CWord[] getWords() {
		return words;
	}
	public void setWords(CWord[] words) {
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
	public List<CWord> getNounWords(){
		List<CWord> wordList = new LinkedList<CWord>();
		for(CWord word:words){
			if(word.getPOS().startsWith("n")){
				wordList.add(word);
			}
		}
		return wordList;
	}
	public List<CWord> getVerbWords(){
		List<CWord> wordList = new LinkedList<CWord>();
		for(CWord word:words){
			if(word.getPOS().startsWith("v")){
				wordList.add(word);
			}
		}
		return wordList;
	}
	
}

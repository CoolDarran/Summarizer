package com.scutdm.summary.doc;

import java.util.List;

public interface Sentence {
	public Word[] getWords();
	public void setWords(Word[] words);
	public int getLength();
	public void setLength(int length);
	public String getSentenceString();
	public void setSentenceString(String sentenceString);
	public String[] getWordClusterStrings();
	public void setWordClusterStrings(String[] wordClusterStrings);
	public boolean isKey();
	public void setKey(boolean isKey);
	public boolean isClassed();
	public void setClassed(boolean isClassed);
	public String getClusterID();
	public void setClusterID(String clusterID);
	public double getScore();
	public void setScore(double score);
	public String getArticleID();
	public void setArticleID(String articleID);
	public String getPosition();
	public void setPosition(String position);
	public void printWords();
	public List<Word> getNounWords();
	public List<Word> getVerbWords();
}

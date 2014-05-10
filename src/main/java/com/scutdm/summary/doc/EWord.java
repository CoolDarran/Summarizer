package com.scutdm.summary.doc;

/**
 * 英文词语类
 * @author wRap
 *
 */

public class EWord{

	private String lemma = "" ;
	private String stem = "";	//词根
	private String POS = "" ;	//词性
	private int sense = 0;	//语义号
	private boolean isKey;
	private boolean isClassed;
	private String clusterID = "";	//词语聚类结果的簇标号
	
	/**
	 * added at 2014.5.7 by Darran.L
	 * to use in WordDBSCAN to store the score of lin between two words
	 * using the current system time as the store ID in the construct function
	 */
	private long storeID;
	
	public long getStoreID() {
		return storeID;
	}
	public EWord(String lemma,String POS){
		setLemma(lemma);
		setPOS(POS);
	}
	public EWord(String lemma, String POS, long andDecrement) {
		setLemma(lemma);
		setPOS(POS);
		this.storeID = System.currentTimeMillis();
	}
	public String getLemma() {
		return lemma;
	}
	public void setLemma(String lemma) {
		this.lemma = lemma;
	}
	public String getStem() {
		return stem;
	}
	public void setStem(String stem) {
		this.stem = stem;
	}
	public String getPOS() {
		return POS;
	}
	public void setPOS(String POS) {
		this.POS = POS;
	}
	public int getSense() {
		return sense;
	}
	public void setSense(int sense) {
		this.sense = sense;
	}
	public boolean isKey() {
		return isKey;
	}
	public void setKey(boolean isKey) {
		this.isKey = isKey;
		this.isClassed=true;
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
	
	
}

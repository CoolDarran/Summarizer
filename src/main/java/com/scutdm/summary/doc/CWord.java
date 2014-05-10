package com.scutdm.summary.doc;

/**
 * 中文词语类
 * @author wRap
 *
 */
public class CWord{

	private String lemma = "" ;
	private String stem = "";	//词根
	private String POS = "" ;	//词性
	private int sense = -1;	//语义号
	private boolean isKey;
	private boolean isClassed;
	private String clusterID = "";	//词语聚类结果的簇标号
	
	public CWord(String lemma,String POS){
		setLemma(lemma);
		setPOS(POS);
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
	public long getStoreID() {
		return 0;
	}
	
	
}

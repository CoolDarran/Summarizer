package com.scutdm.summary.doc;

public interface Word {
	public long getStoreID();
	public String getLemma();
	public void setLemma(String lemma);
	public String getStem();
	public void setStem(String stem);
	public String getPOS();
	public void setPOS(String POS);
	public int getSense();
	public void setSense(int sense);
	public boolean isKey();
	public void setKey(boolean isKey);
	public boolean isClassed();
	public void setClassed(boolean isClassed);
	public String getClusterID();
	public void setClusterID(String clusterID);
}

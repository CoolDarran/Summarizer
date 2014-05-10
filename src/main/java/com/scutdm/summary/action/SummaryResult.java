package com.scutdm.summary.action;

import java.util.List;

public class SummaryResult {
	// 关键词
	private String keyWords;
	// 摘要
	private String summary;
	// url列表
	private List<String> urls;
	// 正文列表
	private List<String> textList;
	// 正文平均字数
	private int avgNum;
	// 正文数目
	private int textSize;
	// 读网页时间
	private long readHTMLTime;
	// 正文抽取时间
	private long textExtractTime;
	// 摘要时间
	private long summarizerTime;
	
	public String getKeyWords() {
		return keyWords;
	}
	public void setKeyWords(String keyWords) {
		this.keyWords = keyWords;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public List<String> getUrls() {
		return urls;
	}
	public void setUrls(List<String> urls) {
		this.urls = urls;
	}
	public int getTextSize() {
		return textSize;
	}
	public void setTextSize(int testSize) {
		this.textSize = testSize;
	}
	public int getAvgNum() {
		return avgNum;
	}
	public void setAvgNum(int avgNum) {
		this.avgNum = avgNum;
	}
	public List<String> getTextList() {
		return textList;
	}
	public void setTextList(List<String> textList) {
		this.textList = textList;
	}
	
	@Override
	public String toString() {
		String newLineChar = System.getProperty("line.separator");
		String output = "Search Word: " + keyWords + newLineChar +"Summary: " + summary.replace(newLineChar, "").replace("\r", "")
				+ newLineChar +"Text Size: " + textSize + newLineChar + "Text Average Words: " + avgNum
				+ newLineChar;
		return output;
	}
	public long getReadHTMLTime() {
		return readHTMLTime;
	}
	public void setReadHTMLTime(long readHTMLTime) {
		this.readHTMLTime = readHTMLTime;
	}
	public long getTextExtractTime() {
		return textExtractTime;
	}
	public void setTextExtractTime(long textExtractTime) {
		this.textExtractTime = textExtractTime;
	}
	public long getSummarizerTime() {
		return summarizerTime;
	}
	public void setSummarizerTime(long summarizerTime) {
		this.summarizerTime = summarizerTime;
	}

}

package com.scutdm.summary.doc;

import com.scutdm.summary.preprocess.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 中文文章类
 * @author wRap
 *
 */
public class CArticle {
	private CSentence[] sentences;	//句子对象
	private int length;
	private String articleString;	//原文
	
	public CArticle(String articleID,String text){
		String s = CUtility.replaceBlank(text);
		String regex = "[!?！？。]";
		String[] senStrings = CUtility.split(s, regex);
		List<CSentence> setenceList = new ArrayList<CSentence>();
		for(int i= 0; i<senStrings.length; i++){
			CSentence sentence = new CSentence(articleID,String.valueOf(i),senStrings[i]);
			setenceList.add(sentence);
		}
		CSentence[] sentenceArray = new CSentence[setenceList.size()];
		setSentences(setenceList.toArray(sentenceArray));
		setLength(getSentences().length);
		setArticleString(text);
	}
	public CSentence[] getSentences() {
		return sentences;
	}
	public void setSentences(CSentence[] sentences) {
		this.sentences = sentences;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public String getArticleString() {
		return articleString;
	}
	public void setArticleString(String articleString) {
		this.articleString = articleString;
	}
}

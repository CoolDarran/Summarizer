package com.scutdm.summary.summary;

import java.util.ArrayList;
import java.util.List;

import com.scutdm.summary.doc.ESentence;

/**
 * 英文
 * 子主题类，储存句子聚类结果，每个簇一个子主题
 * @author wRap
 *
 */

public class ESubTopic {
	private List<ESentence> sentences = new ArrayList<ESentence>();	//包含的句子
	private int articleCount = 0;	//包含的句子所属的文章数
	private Integer score = 0;	//子主题得分
	
	public ESubTopic(List<ESentence> sentences,int articleCount){
		this.sentences = sentences;
		this.articleCount = articleCount;
		calScore();
	}
	public List<ESentence> getSentences() {
		return sentences;
	}
	public void setSentences(List<ESentence> sentences) {
		this.sentences = sentences;
	}
	public int getArticleCount() {
		return articleCount;
	}
	public void setArticleCount(int articleCount) {
		this.articleCount = articleCount;
	}
	public Integer getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	private void calScore(){
		int score = getArticleCount() * sentences.size();
		setScore(score);
	}
	//返回子主题中得分最高的句子
	public String getSummarySent(){
		double max = 0;
		String summarySent = "";
		for(ESentence sentence:getSentences()){
			if(sentence.getScore() > max){
				max = sentence.getScore();
				summarySent = sentence.getSentenceString().trim();
			}
		}
		return summarySent;
	}
	

}

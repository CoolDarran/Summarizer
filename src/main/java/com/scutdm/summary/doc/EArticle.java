package com.scutdm.summary.doc;

import java.util.ArrayList;
import java.util.List;

import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * 英文文章类
 * @author wRap
 *
 */
public class EArticle {
	private ESentence[] sentences;	//句子对象
	private int length;
	private String articleString;	//原文
	
	public EArticle(String articleID,String text,TokenizerFactory TOKENIZER_FACTORY,SentenceModel SENTENCE_MODEL,MaxentTagger tagger){
		/*
		 * 对文章分句
		 */
		ArrayList tokenList = new ArrayList();
		ArrayList whiteList = new ArrayList();
		Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(text.toCharArray(),0,text.length());
		tokenizer.tokenize(tokenList,whiteList);

//		System.out.println(tokenList.size() + " TOKENS");
//		System.out.println(whiteList.size() + " WHITESPACES");

		String[] tokens = new String[tokenList.size()];
		String[] whites = new String[whiteList.size()];
		tokenList.toArray(tokens);
		whiteList.toArray(whites);
		int[] sentenceBoundaries = SENTENCE_MODEL.boundaryIndices(tokens,whites);

//		System.out.println(sentenceBoundaries.length  + " SENTENCE END TOKEN OFFSETS");
			
		if (sentenceBoundaries.length < 1) {
		    System.out.println("No sentence boundaries found.");
		    return;
		}
		int sentStartTok = 0;
		int sentEndTok = 0;
		String sentence = "";
		List<ESentence> setenceList = new ArrayList<ESentence>();
		
		for (int i = 0; i < sentenceBoundaries.length; ++i) {
		    sentEndTok = sentenceBoundaries[i];
//		    System.out.println("SENTENCE "+(i+1)+": ");
		    sentence = "";
		    for (int j=sentStartTok; j<=sentEndTok; j++) {
//		    	System.out.print(tokens[j]+whites[j+1]);
		    	sentence += tokens[j]+whites[j+1];
		    }
		    ESentence sentenceObj = new ESentence(articleID,String.valueOf(i),tagger,sentence);
//		    System.out.println();
//		    System.out.println(sentenceObj.getSentenceString());
//		    sentenceObj.printWords();
		    setenceList.add(sentenceObj);
		    sentStartTok = sentEndTok+1;
		}
		ESentence[] sentenceArray = new ESentence[setenceList.size()];
		setSentences(setenceList.toArray(sentenceArray));
		setLength(getSentences().length);
		setArticleString(text);
	}
	public ESentence[] getSentences() {
		return sentences;
	}
	public void setSentences(ESentence[] sentences) {
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

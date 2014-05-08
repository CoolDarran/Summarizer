package com.scutdm.summary.action;

import java.net.URL;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.Preparable;
import com.scutdm.summary.helper.SearchHelper;
import com.scutdm.summary.utility.Utility;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.sussex.nlp.jws.JWS;

/**
 * get keywords and search it
 * @author L.danran
 * Created on Apr 3, 2014
 */
public class SearchAction extends ActionSupport implements Preparable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5304870503570259994L;
	
	private JWS ws;
	private IDictionary dict;
	
	private String keyWords;
	private SummaryResult sum = new SummaryResult();

	public String getKeyWords() {
		return keyWords;
	}

	public void setKeyWords(String keyWords) {
		this.keyWords = keyWords;
	}
	
	/**
	 * ËÑË÷¹Ø¼ü×Ö
	 * 
	 * @return
	 */
	public String searchWords(){
		System.out.println(keyWords);
		try {
			setSum(SearchHelper.searchGoogle(keyWords, ws, dict));
			Utility.printTimeElapsed(getSum());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return SUCCESS;
	}

	public SummaryResult getSum() {
		return sum;
	}

	public void setSum(SummaryResult sum) {
		this.sum = sum;
	}

	public void prepare() throws Exception {
		this.ws = Instance.WS;
		URL wordnetDir = new URL("file",null,"./WordNet/2.1/dict");
        Instance.setDict(new Dictionary(wordnetDir));
        this.dict = Instance.getDict();
	}
}

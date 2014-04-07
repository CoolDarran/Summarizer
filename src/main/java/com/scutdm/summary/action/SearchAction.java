package com.scutdm.summary.action;

import java.util.ArrayList;
import java.util.List;

import com.opensymphony.xwork2.ActionSupport;
import com.scutdm.summary.helper.SearchHelper;

/**
 * get keywords and search it
 * @author L.danran
 * Created on Apr 3, 2014
 */
public class SearchAction extends ActionSupport{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5304870503570259994L;
	
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
			setSum(SearchHelper.searchGoogle(keyWords));
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
}

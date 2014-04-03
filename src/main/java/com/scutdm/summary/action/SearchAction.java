package com.scutdm.summary.action;

import com.opensymphony.xwork2.ActionSupport;

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

	public String getKeyWords() {
		return keyWords;
	}

	public void setKeyWords(String keyWords) {
		this.keyWords = keyWords;
	}
	
	/**
	 * 搜索关键字
	 * 
	 * @return
	 */
	public String searchWords(){
		System.out.println(keyWords);
		
		return SUCCESS;
	}
}

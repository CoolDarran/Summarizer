package com.scutdm.summary.helper;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLEncoder;

import com.google.gson.Gson;

/**
 * search keywords in google news
 * google search api could only get 4 results once a time
 * 
 * @author L.danran
 * Created on Apr 4, 2014
 */
public class SearchHelper {
	public static void searchGoogle(String keyWords) throws Exception{
		int totalSize = 10;
		for(int i = 0; i < totalSize;){
			// Google news api was deprecated
			// TODO consider to use Google News RSS api or use Bing News Search api
			// http://news.google.com/news?q=[keywords]&output=rss
			String address = "http://ajax.googleapis.com/ajax/services/search/news?v=1.0&start="+i+"&q=";
			String query = keyWords;
			String charset = "UTF-8";
 
			URL url = new URL(address + URLEncoder.encode(query, charset));
			Reader reader = new InputStreamReader(url.openStream(), charset);
			GoogleResults results = new Gson().fromJson(reader, GoogleResults.class);
 
			int total = results.getResponseData().getResults().size();
 
			// Show title and URL of each results
			for(int j=0; j < total && i < totalSize; j++,i++){
				System.out.println("Title: " + results.getResponseData().getResults().get(j).getTitle());
				System.out.println("URL: " + results.getResponseData().getResults().get(j).getUrl() + "\n");
			}
		}
	}
	
	public static void main(String[] argc) throws Exception{
		searchGoogle("manchester bayern");
	}
}

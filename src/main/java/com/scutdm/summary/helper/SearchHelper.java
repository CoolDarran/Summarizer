package com.scutdm.summary.helper;

import java.util.ArrayList;
import java.util.List;

import com.scutdm.summary.extract.ReadHTML;
import com.scutdm.summary.extract.TextExtract;
import com.scutdm.summary.extract.WriteUrl2Txt;
import com.scutdm.summary.rss.Feed;
import com.scutdm.summary.rss.FeedMessage;
import com.scutdm.summary.rss.RSSFeedParser;

/**
 * search keywords in google news
 * google search api could only get 4 results once a time
 * 
 * @author L.danran
 * Created on Apr 4, 2014
 */
public class SearchHelper {
	private static TextExtract textExtact = new TextExtract();
	private static WriteUrl2Txt wr2Txt = new WriteUrl2Txt();
	
	public static void searchGoogle(String keyWords) throws Exception{
		List<String> urls = new ArrayList<String>();
		int totalSize = 10;
		
		// get 10 news urls
		RSSFeedParser parser = new RSSFeedParser(keyWords);
		Feed feed = parser.readFeed();
		System.out.println(feed);
		for (FeedMessage message : feed.getMessages().subList(0, totalSize)) {
			System.out.println(message);
			urls.add(message.getLink());
		}		 				               
		
		// get the text of these 10 news urls
		List<String> htmlContents = ReadHTML.readHtml(urls);
		int i = 1;
		
		// use boilerplate - Shallow Text Features to extract text 
//		for(String text : htmlContents){
//			if (text != null && !text.equals("")) {
//	        	wr2Txt.write(keyWords+i, text); //将逻辑给到了IO层！！！
//	        }
//			i++;
//		}
		

		for(String content : htmlContents){
			//实现正文抽取
	        String parseText = textExtact.parse(content);
	        System.out.println(parseText);	
	        if (parseText != null && !parseText.equals("")) {
	        	wr2Txt.write(keyWords+i, parseText); //将逻辑给到了IO层！！！
	        }
	        i++;	      	        
		}
		
	}		
	
	public static void main(String[] argc) throws Exception{
		searchGoogle("文章 马伊俐");	
	
	}
}

package com.scutdm.summary.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.scutdm.summary.action.SummaryResult;
import com.scutdm.summary.extract.Check;
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
	private static WriteUrl2Txt wr2Txt = new WriteUrl2Txt();
	private static SummarizerHelper sumHelper = new SummarizerHelper();
	private static TextExtract txtExtract = new TextExtract();
	private static SummaryResult sum = new SummaryResult();
	
	public static SummaryResult searchGoogle(String keyWords) throws Exception{
		
		// set goagent proxy
		System.setProperty("http.proxySet", "true");
		System.setProperty("http.proxyHost", "127.0.0.1");
		System.setProperty("http.proxyPort", "8087");
		
		if(Check.isChinese(keyWords)){
			System.setProperty("file.encoding", "gb2312");
		}else{
			System.setProperty("file.encoding", "GBK");
		}
		
		// store fetch urls
		List<String> urls = new ArrayList<String>();
		List<String> titleAndUrls = new ArrayList<String>();
		// store extracted text
		List<String> textList = new ArrayList<String>();
		// total url size
		int totalSize = 10;
		
		// get 10 news urls
		RSSFeedParser parser = new RSSFeedParser(Check.isChinese(keyWords),keyWords);
		Feed feed = parser.readFeed();
		System.out.println(feed);
		for (FeedMessage message : feed.getMessages()) {
			if(urls.indexOf("people.com.cn") == -1){
				System.out.println(message);
				urls.add(message.getLink());
				titleAndUrls.add(message.getTitle() + "," + message.getLink());
			}
			if(urls.size() > totalSize)
				break;
		}		 				               
		
		int i = 1;
		
		List<String> htmlContents = new ArrayList<String>();
		// get the text of these 10 news urls
		if(Check.isChinese(keyWords)){
			
			System.out.println("Start extractor");
			textList.addAll(ReadHTML.readChinHtmlB(urls));
			System.out.println("End extractor");
//			for(String text : textList){
//				if (text != null && !text.equals("")) {
//		        	wr2Txt.write(keyWords+i, text); //将逻辑给到了IO层！！！
//		        }
//				i++;
//			}
			
			// Chinese  use 基于行快分布
//			htmlContents = ReadHTML.readChinHtml(urls);
//			for(String content : htmlContents){
//				// 实现正文抽取
//		        String parseText = txtExtract.parse(content);
//		        textList.add(parseText);//set(i-1, textList.get(i-1) + "\n" + parseText);
//		        System.out.println("text using 行块： " + parseText);	
//		        if (parseText != null && !parseText.equals("")) {
//		        	wr2Txt.write(keyWords+i, parseText); //将逻辑给到了IO层！！！
//		        }
//		        i++;
//			}
						
		}
		else{
			//  use boilerplate - Shallow Text Features to extract text 
			htmlContents = ReadHTML.readEngHtml(urls);
			textList.addAll(htmlContents);
//			for(String text : htmlContents){
//				if (text != null && !text.equals("")) {
//		        	wr2Txt.write(keyWords+i, text); //将逻辑给到了IO层！！！
//		        }
//				i++;
//			}
		}
		System.out.println("Start summary");
		// pass extracted text to summarizer
		String summary = sumHelper.passText(Check.isChinese(keyWords), keyWords, textList);
		System.out.println("Summary: " + summary);
		
		sum.setSummary(summary);
		sum.setKeyWords(keyWords);
		sum.setUrls(titleAndUrls);
		return sum;
	}		
	
	public static void main(String[] argc) throws Exception{
		searchGoogle("我是歌手");	
	}
}

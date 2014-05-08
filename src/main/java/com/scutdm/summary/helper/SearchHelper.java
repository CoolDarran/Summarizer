package com.scutdm.summary.helper;

import java.util.ArrayList;
import java.util.List;

import com.scutdm.summary.action.SummaryResult;
import com.scutdm.summary.extract.Check;
import com.scutdm.summary.extract.ReadHTML;
import com.scutdm.summary.rss.Feed;
import com.scutdm.summary.rss.FeedMessage;
import com.scutdm.summary.rss.RSSFeedParser;

import edu.mit.jwi.IDictionary;
import edu.sussex.nlp.jws.JWS;

/**
 * search keywords in google news
 * google search api could only get 4 results once a time
 * 
 * @author L.danran
 * Created on Apr 4, 2014
 */
public class SearchHelper {
//	private static WriteUrl2Txt wr2Txt = new WriteUrl2Txt();
	private static SummarizerHelper sumHelper = new SummarizerHelper();
//	private static TextExtract txtExtract = new TextExtract();
	private static SummaryResult sum = new SummaryResult();
	
	public static SummaryResult searchGoogle(String keyWords, JWS ws, IDictionary dict) throws Exception{
		
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
		long readHTMLS = System.currentTimeMillis();
		RSSFeedParser parser = new RSSFeedParser(Check.isChinese(keyWords),keyWords);
		Feed feed = parser.readFeed();
		for (FeedMessage message : feed.getMessages()) {
			// 去除访问不到、抽取不到正以及不能在1s内访问到的网站
			if(urls.indexOf("people.com.cn") == -1){
				urls.add(message.getLink());
				titleAndUrls.add(message.getTitle() + "," + message.getLink());
			}
			if(urls.size() >= totalSize)
				break;
		}		
		long readHTMLE = System.currentTimeMillis();
		sum.setReadHTMLTime(readHTMLE - readHTMLS);
		
		long textExtractS = System.currentTimeMillis();
		// get the text of these 10 news urls
		System.out.println("Start extractor for " + keyWords);
		textList.addAll(ReadHTML.pExtractText(urls, keyWords));
		System.out.println("End extractor for " + keyWords);
		long textExtractE = System.currentTimeMillis();
		sum.setTextExtractTime(textExtractE - textExtractS);
		
		System.out.println("Start summary for " + keyWords);
		long summarizerS = System.currentTimeMillis();
		// pass extracted text to summarizer
		String summary = sumHelper.passText(Check.isChinese(keyWords), keyWords, textList,ws,dict);
		long summarizerE = System.currentTimeMillis();
		System.out.println("End summary for " + keyWords);
		sum.setSummarizerTime(summarizerE - summarizerS);
		System.out.println("Summary: " + summary);
		
		sum.setTextSize(textList.size());
		sum.setTextList(textList);
		sum.setSummary(summary);
		sum.setKeyWords(keyWords);
		sum.setUrls(titleAndUrls);
		return sum;
	}

	public static SummaryResult doSummarizer(String keyWords, JWS ws, IDictionary dict) {
		if(Check.isChinese(keyWords)){
			System.setProperty("file.encoding", "gb2312");
		}else{
			System.setProperty("file.encoding", "GBK");
		}
		
		// store extracted text
		List<String> textList = new ArrayList<String>();
		
		long textExtractS = System.currentTimeMillis();
		// get the text of these 10 news urls
		
			System.out.println("Start extractor for " + keyWords);
			textList.addAll(ReadHTML.extractFromFile(keyWords));
			System.out.println("End extractor for " + keyWords);
			
			
		long textExtractE = System.currentTimeMillis();
		sum.setTextExtractTime(textExtractE - textExtractS);
		
		System.out.println("Start summary for " + keyWords);
		long summarizerS = System.currentTimeMillis();
		// pass extracted text to summarizer
		String summary = sumHelper.passText(Check.isChinese(keyWords), keyWords, textList,ws,dict);
		long summarizerE = System.currentTimeMillis();
		System.out.println("End summary for " + keyWords);
		sum.setSummarizerTime(summarizerE - summarizerS);
		System.out.println("Summary: " + summary);
		
		sum.setTextSize(textList.size());
		sum.setTextList(textList);
		sum.setSummary(summary);
		sum.setKeyWords(keyWords);
		return sum;
	}		
	
}

package com.scutdm.summary.rss;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * RSS reader
 * 
 * @author L.danran
 * Created on Apr 4, 2014
 */
public class RSSFeedParser {
	static final String TITLE = "title";
	static final String DESCRIPTION = "description";
	static final String CHANNEL = "channel";
	static final String LANGUAGE = "language";
	static final String COPYRIGHT = "copyright";
	static final String LINK = "link";
	static final String AUTHOR = "author";
	static final String ITEM = "item";
	static final String PUB_DATE = "pubDate";
	static final String GUID = "guid";

	final URL url;
	
	public RSSFeedParser(boolean chinese, String feedKeyWords) {
		try {
			if(chinese)
				this.url = new URL("http://news.google.com/news?q=" + URLEncoder.encode(feedKeyWords, "UTF-8") +"&ned=cn&hl=zh-CN&output=rss");
			else
				this.url = new URL("http://news.google.com/news?q=" + URLEncoder.encode(feedKeyWords, "UTF-8") +"&output=rss");
	    } catch (Exception e) {
	    	throw new RuntimeException(e);
	    }
	}
	
	/**
	 * using rome to implementing parsing RSS feeds
	 * 
	 * @return
	 */
	public Feed readFeed(){
		Feed feed = null;
		
		XmlReader reader = null;
		try{
			reader = new XmlReader(url);
			SyndFeed feeds = new SyndFeedInput().build(reader);
			
			// feed header
			feed = new Feed(feeds.getTitle(), feeds.getLink(), feeds.getDescription(), feeds.getLanguage(),
					feeds.getCopyright(), feeds.getPublishedDate().toString());
			
			for(Iterator<?> it = feeds.getEntries().iterator(); it.hasNext();){
				SyndEntry entry = (SyndEntry) it.next();
//				System.out.println(entry.getTitle());
				FeedMessage message = new FeedMessage();
				message.setAuthor(entry.getAuthor());
				message.setDescription(entry.getDescription().toString());
				message.setLink(entry.getUri().split(",")[1].split("=")[1]);
				message.setTitle(entry.getTitle());
				feed.getMessages().add(message);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (FeedException e) {
			e.printStackTrace();
		} finally{
			if(reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return feed;
	}
}

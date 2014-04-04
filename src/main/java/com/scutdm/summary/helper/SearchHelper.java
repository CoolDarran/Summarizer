package com.scutdm.summary.helper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.log4j.Logger;

import com.scutdm.summary.crawler.core.Cral2FileController;
import com.scutdm.summary.crawler.core.CrawlProperties;
import com.scutdm.summary.crawler.io.URlSeedFileMgt;
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
	public static void searchGoogle(String keyWords) throws Exception{
		List<String> urls = new ArrayList<String>();
		String seedPath = "urls.txt";
		File seedFile = null;
		BufferedWriter writer = null;
		int totalSize = 10;
		/**
		for(int i = 0; i < totalSize;){
			// Google news api was deprecated
			// consider to use Google News RSS api or use Bing News Search api
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
		**/
		
		// get 10 news urls
		RSSFeedParser parser = new RSSFeedParser(keyWords);
		Feed feed = parser.readFeed();
		System.out.println(feed);
		for (FeedMessage message : feed.getMessages().subList(0, totalSize)) {
			System.out.println(message);
			urls.add(message.getGuid());
		}
		
		// 写入urls文件				        
        if (CrawlProperties.contains("crawl.url.seeds")) {
            seedPath = CrawlProperties.getProperty("crawl.url.seeds");
        }
        try {
            System.out.println(URlSeedFileMgt.class.getResource("").toURI().getPath()+seedPath);
            seedFile = new File(URlSeedFileMgt.class.getResource("").toURI().getPath()+seedPath);
        } catch (URISyntaxException ex) {
           Logger.getRootLogger().error(ex);
        }
        
        try {
            writer = new BufferedWriter(new FileWriter(seedFile));
            for(String url : urls){
            	writer.write(url);
            	writer.newLine();
            }

        } catch (FileNotFoundException ex) {
            Logger.getRootLogger().error(ex);
        } catch (IOException ex) {
            Logger.getRootLogger().error(ex);
        } finally {
            if (writer != null) {
                try {
                	writer.close();
                } catch (IOException ex) {
                   Logger.getRootLogger().error(ex);
                }
            }
        }
		
		// TODO get the text of these 10 news urls
		try {
            new Cral2FileController().startCrawl();
        } catch (Exception ex) {
        	java.util.logging.Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
	
	public static void main(String[] argc) throws Exception{
		searchGoogle("马航");
	}
}

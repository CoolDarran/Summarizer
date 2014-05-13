package com.scutdm.summary.extract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.scutdm.summary.preprocess.CUtility;
import com.scutdm.summary.utility.CommonUtility;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;

/**
 * read html for extracting text
 * 
 * @author L.danran
 * Created on Apr 4, 2014
 */
public class ReadHTML {
	
	// extracted texts
	private static List<String> texts;
	
	/**
	 * read html content according to the list of urls
	 * 使用Boilerplate进行正文抽取
	 * @param urls
	 * @return
	 */
	public static List<String> extractText(List<String> urls) {
		texts = new ArrayList<String>();
		
		// Create an HttpClient with the ThreadSafeClientConnManager.
        // This connection manager must be used if more than one thread will
        // be using the HttpClient.
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(100);

        CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(cm).build();
		
		synchronized (texts) {
			try {
				// create a thread for each URI
				ExtractThread[] threads = new ExtractThread[urls.size()];
				for (int i = 0; i < threads.length; i++) {
					HttpGet httpget = new HttpGet(urls.get(i));
					threads[i] = new ExtractThread(httpclient, httpget, i + 1);
				}
				// start the threads
				for (int j = 0; j < threads.length; j++) {
					threads[j].start();
				}
				// join the threads
				for (int j = 0; j < threads.length; j++) {
					threads[j].join();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				try {
					httpclient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return texts;
	}

	/**
	 * 从文档读html源文件
	 * 在html_src目录下
	 * @param keyWords
	 * @return
	 */
	public static Collection<? extends String> extractFromFile(String keyWords) {
		String[] paths = CUtility.getPath("html_src/");		//文档集合目录
		List<String> textList = new ArrayList<String>();
		
		for(int i=0; i<paths.length; i++){
			if(paths[i].indexOf(keyWords) != -1 && keyWords!=" "){
			String text = CUtility.readFile(paths[i]);
			try {
				text = ArticleExtractor.INSTANCE.getText(text);
			} catch (BoilerpipeProcessingException e) {
				System.out.println("Cannot extract text!!!!! file: " + paths[i]);
				text = " ";
			}
			textList.add(text);
			}
		}
		return textList;
	}
	
	/**
	 * 多线程 读取HTML并抽取正文
	 * @author danran
	 * Created on 2014年5月10日
	 */
	static class ExtractThread extends Thread {

        private final CloseableHttpClient httpClient;
        private final HttpContext context;
        private final HttpGet httpget;
        private final int id;

        public ExtractThread(CloseableHttpClient httpClient, HttpGet httpget, int id) {
            this.httpClient = httpClient;
            this.context = new BasicHttpContext();
            this.httpget = httpget;
            this.id = id;
        }

        /**
         * Executes the GetMethod and extract the text from html.
         */
        @Override
        public void run() {
            try {
                CloseableHttpResponse response = httpClient.execute(httpget, context);
                try {
                    // get the response body as an array of bytes
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        byte[] bytes = EntityUtils.toByteArray(entity);
                        String text = ArticleExtractor.INSTANCE.getText(new String(bytes,"utf-8"));
                        texts.add(text);
                        System.out.println(text);
                    }
                } finally {
                    response.close();
                }
            } catch (Exception e) {
                System.out.println(id + " - error: " + e);
            }
        }
    }
	
	public static List<String> pExtractText(List<String> urls, String keyWords) {
		texts = new ArrayList<String>();
		int splitSize = (int) Math.floor(urls.size()/5.0);
		synchronized(texts){
			Thread t1 = new Thread(new runExtract(urls.subList(0, splitSize),keyWords));
			Thread t2 = new Thread(new runExtract(urls.subList(splitSize, 2*splitSize),keyWords));
			Thread t3 = new Thread(new runExtract(urls.subList(2*splitSize, 3*splitSize),keyWords));
			Thread t4 = new Thread(new runExtract(urls.subList(3*splitSize, 4*splitSize),keyWords));
			Thread t5 = new Thread(new runExtract(urls.subList(4*splitSize, urls.size()),keyWords));
			t1.start();
			t2.start();
			t3.start();
			t4.start();
			t5.start();
			try {
				t1.join();
				t2.join();
				t3.join();
				t4.join();
				t5.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return texts;
	}
	
	public static String processPage(String urlText, String keyWords){
		String text = "";
		try {
			URL url = new URL(urlText);
			URLConnection con = url.openConnection();
			// time out 1s
			con.setConnectTimeout(1000);
			Pattern p = Pattern.compile("text/html;\\s*charset=([^\\s]+)\\s*");
			Matcher m;
			if(con.getContentType()!=null)
				m = p.matcher(con.getContentType());
			else
				m = p.matcher(" ");
			/* If Content-Type doesn't match this pre-conception, choose default "gb2312" and 
			 * hope for the best. */
			String charset = m.matches() ? m.group(1) : CommonUtility.checkCharset(url);
			
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), Charset.forName(charset)));
			
			text = ArticleExtractor.INSTANCE.getText(in);
			
			System.out.println("Extracted text: " + text);
		} catch (MalformedURLException e) {
			Logger.getLogger(ReadHTML.class.getName()).log(Level.ERROR, null, e);
			return " ";
		} catch (BoilerpipeProcessingException e) {
			System.out.println("Cannot extract text!!!!! site: " + urlText);
			Logger.getLogger(ReadHTML.class.getName()).log(Level.ERROR, null, e);
			return " ";
		} catch (IOException e) {
			System.out.println("Cannot read html !!!!! site: " + urlText);
			Logger.getLogger(ReadHTML.class.getName()).log(Level.ERROR, null, e);
			return " ";
		}
		return text;
	}
	
	static class runExtract implements Runnable{
		private List<String> urls;
		private String keyWords;
		runExtract(List<String> urls, String keyWord) { this.urls = urls; this.keyWords = keyWord;}
		public void run() {
			for (String urlText : urls) {
				// use boilerplate - Shallow Text Features to extract text
				String contents = processPage(urlText, keyWords);
				texts.add(contents);
			}
		}
	}
	
}

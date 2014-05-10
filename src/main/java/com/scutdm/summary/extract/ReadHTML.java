package com.scutdm.summary.extract;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.scutdm.summary.preprocess.CUtility;

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
                        texts.add(ArticleExtractor.INSTANCE.getText(new String(bytes,"utf-8")));
                    }
                } finally {
                    response.close();
                }
            } catch (Exception e) {
                System.out.println(id + " - error: " + e);
            }
        }

    }
	
}

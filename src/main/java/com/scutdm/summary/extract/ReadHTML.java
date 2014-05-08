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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ictclas4j.utility.GFString;

import com.wrap.chinsummarizer.preprocess.Utility;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;

/**
 * read html for extracting text
 * 
 * @author L.danran
 * Created on Apr 4, 2014
 */
public class ReadHTML {
	
	private static List<String> htmlContents;
	
	/**
	 * read html content according to the list of urls
	 * 使用Boilerplate
	 * @param urls
	 * @param keyWords 
	 * @return
	 */
	public static List<String> extractText(List<String> urls, String keyWords) {
		List<String> htmlContents = new ArrayList<String>();
		for (String urlText : urls) {
			// use boilerplate - Shallow Text Features to extract text
			String contents = processPage(urlText,keyWords);
			htmlContents.add(GFString.getEncodedString(contents.getBytes(),"gb2312"));
		}
		return htmlContents;
	}
	
	public static List<String> pExtractText(List<String> urls, String keyWords) {
		htmlContents = new ArrayList<String>();
		int size = urls.size()/5;
		synchronized(htmlContents){
			Thread t1 = new Thread(new runExtract(urls.subList(0, size),keyWords));
			Thread t2 = new Thread(new runExtract(urls.subList(size, 2*size),keyWords));
			Thread t3 = new Thread(new runExtract(urls.subList(2*size, 3*size),keyWords));
			Thread t4 = new Thread(new runExtract(urls.subList(3*size, 4*size),keyWords));
			Thread t5 = new Thread(new runExtract(urls.subList(4*size, urls.size()),keyWords));
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
		return htmlContents;
	}
	
	/**
	 *  "Boilerplate Detection using Shallow Text Features" by Christian Kohlschütter et al., presented at WSDM 2010
	 *  https://code.google.com/p/boilerpipe/
	 *  抽取正文段
	 * 
	 * @param urlText
	 * @param keyWords 
	 * @return
	 */
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
			String charset = m.matches() ? m.group(1) : Check.checkCharset(url);
			
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), Charset.forName(charset)));
			
			text = ArticleExtractor.INSTANCE.getText(in);
			
			System.out.println("Extracted text: " + text);
		} catch (MalformedURLException e) {
			Logger.getLogger(ReadHTML.class.getName()).log(Level.SEVERE, null, e);
			return " ";
		} catch (BoilerpipeProcessingException e) {
			System.out.println("Cannot extract text!!!!! site: " + urlText);
			Logger.getLogger(ReadHTML.class.getName()).log(Level.SEVERE, null, e);
			return " ";
		} catch (IOException e) {
			System.out.println("Cannot read html !!!!! site: " + urlText);
			Logger.getLogger(ReadHTML.class.getName()).log(Level.SEVERE, null, e);
			return " ";
		}
		return text;
	}

	/**
	 * 从文档读html源文件
	 * 在html_src目录下
	 * @param keyWords
	 * @return
	 */
	public static Collection<? extends String> extractFromFile(String keyWords) {
		String[] paths = Utility.getPath("html_src/");		//文档集合目录
		List<String> textList = new ArrayList<String>();
		
		for(int i=0; i<paths.length; i++){
			if(paths[i].indexOf(keyWords) != -1 && keyWords!=" "){
			String text = Utility.readFile(paths[i]);
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
	 * 
	 * @author danran
	 * Created on 2014年5月8日
	 */
	static class runExtract implements Runnable{
		private List<String> urls;
		private String keyWords;
		runExtract(List<String> urls, String keyWord) { this.urls = urls; this.keyWords = keyWord;}
		public void run() {
			for (String urlText : urls) {
				// use boilerplate - Shallow Text Features to extract text
				String contents = processPage(urlText, keyWords);
				htmlContents.add(GFString.getEncodedString(contents.getBytes(),"gb2312"));
			}
		}
		
	}
}

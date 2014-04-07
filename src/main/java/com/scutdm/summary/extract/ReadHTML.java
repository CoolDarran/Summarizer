package com.scutdm.summary.extract;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ictclas4j.utility.GFString;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import de.l3s.boilerpipe.extractors.DefaultExtractor;

/**
 * read html for extracting text
 * 
 * @author L.danran
 * Created on Apr 4, 2014
 */
public class ReadHTML {
	
	public static final int UNSUPPORT_TYPE = 1;
    public static final int FILE_NOT_EXIT = 2;
    public static String resDir;
	
	static {      
        resDir = "html_result/";
        
        File file = new File(resDir);
        if (!file.exists()) {
            file.mkdirs();
        }
    }
	
	/**
	 * read chinese html content according to the list of urls
	 * 基于行块分布
	 * 
	 * @param urls
	 * @return
	 */
	public static List<String> readChinHtml(List<String> urls){		
		
		// set goagent proxy
		System.setProperty("http.proxySet", "true");
		System.setProperty("http.proxyHost", "127.0.0.1");
		System.setProperty("http.proxyPort", "8087"); 
		
		List<String> htmlContents = new ArrayList<String>();
		BufferedReader in = null;
		int i = 1;
		for(String urlText : urls){
			try{
				
				// read html content
				URL url = new URL(urlText);
				URLConnection con = url.openConnection();
				Pattern p = Pattern.compile("text/html;\\s*charset=([^\\s]+)\\s*");
				Matcher m = p.matcher(con.getContentType());
				/* If Content-Type doesn't match this pre-conception, choose default and 
				 * hope for the best. */
				String charset = m.matches() ? m.group(1) : Check.checkCharset(url);
				
				System.out.println("Content type: " + con.getContentType());
				System.out.println("Match charset: " + charset);	
				
				in = new BufferedReader(new InputStreamReader(url.openStream(), Charset.forName(charset)));
				String line = null;
				StringBuilder sb = new StringBuilder();
				while((line = in.readLine())!=null){
					sb.append(line);
					sb.append(System.getProperty("line.separator"));
				}
				htmlContents.add(GFString.getEncodedString(sb.toString().getBytes(),"gb2312"));
				System.out.println("HTML content: " + sb.toString());																
				
				// write to file
				File file = new File(resDir + i + ".txt");
		        //file.deleteOnExit();
		        Writer pw = null;
		        try {
		            pw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
		            pw.write(sb.toString());
		            pw.flush();

		        } catch (IOException ex) {
		            Logger.getLogger(WriteUrl2Txt.class.getName()).log(Level.SEVERE, null, ex);
		        } finally {
		            if (pw != null) {
		                pw.close();
		            }
		        }
		        i++;
				
			} catch (Exception e) {
			      e.printStackTrace();
		    } finally {
		      if (in != null) {
		        try {
		          in.close();
		        } catch (IOException e) {
		          e.printStackTrace();
		        }
		      }
		    }
		}
		return htmlContents;		
	}
	
	/**
	 * read html content according to the list of urls
	 * 
	 * @param urls
	 * @return
	 */
	public static List<String> readEngHtml(List<String> urls){		
		
		// set goagent proxy
		System.setProperty("http.proxySet", "true");
		System.setProperty("http.proxyHost", "127.0.0.1");
		System.setProperty("http.proxyPort", "8087"); 
		
		List<String> htmlContents = new ArrayList<String>();
		int i = 1;
		for(String urlText : urls){
			// use boilerplate - Shallow Text Features to extract text 
			String contents = processPage(urlText);
			htmlContents.add(GFString.getEncodedString(contents.getBytes(),"UTF-8"));
			
			// write to file
//			File file = new File(resDir + i + ".txt");
//	        //file.deleteOnExit();
//	        Writer pw = null;
//	        try {
//	            pw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
//	            pw.write(contents);
//	            pw.flush();
//
//	        } catch (IOException ex) {
//	            Logger.getLogger(WriteUrl2Txt.class.getName()).log(Level.SEVERE, null, ex);
//	        } finally {
//	            if (pw != null) {
//	                try {
//						pw.close();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//	            }
//	        }
//	        i++;
		}
		return htmlContents;
	}
	
	/**
	 *  "Boilerplate Detection using Shallow Text Features" by Christian Kohlschütter et al., presented at WSDM 2010
	 *  https://code.google.com/p/boilerpipe/
	 *  抽取正文段
	 * 
	 * @param urlText
	 * @return
	 */
	public static String processPage(String urlText){
		String text = "";
		try {
			URL url = new URL(urlText);
			URLConnection con = url.openConnection();
			// time out 1s
			con.setConnectTimeout(1000);
			Pattern p = Pattern.compile("text/html;\\s*charset=([^\\s]+)\\s*");
			Matcher m = p.matcher(con.getContentType());
			/* If Content-Type doesn't match this pre-conception, choose default and 
			 * hope for the best. */
			String charset = m.matches() ? m.group(1) : Check.checkCharset(url);
			
			System.out.println("Content type: " + con.getContentType());
			System.out.println("Match charset: " + charset);	
			
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), Charset.forName(charset)));
			
			text = ArticleExtractor.INSTANCE.getText(in);
			
			System.out.println("Extracted text: " + text);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (BoilerpipeProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return text;
		
	}

	/**
	 * read chinese html content according to the list of urls
	 * 使用Boilerplate
	 * @param urls
	 * @return
	 */
	public static List<String> readChinHtmlB(List<String> urls) {
		// set goagent proxy
		System.setProperty("http.proxySet", "true");
		System.setProperty("http.proxyHost", "127.0.0.1");
		System.setProperty("http.proxyPort", "8087");

		List<String> htmlContents = new ArrayList<String>();
		int i = 1;
		for (String urlText : urls) {
			// use boilerplate - Shallow Text Features to extract text
			String contents = processPage(urlText);
			htmlContents.add(GFString.getEncodedString(contents.getBytes(),
					"gb2312"));

//			// write to file
//			File file = new File(resDir + i + ".txt");
//			// file.deleteOnExit();
//			Writer pw = null;
//			try {
//				pw = new BufferedWriter(new OutputStreamWriter(
//						new FileOutputStream(file), "gb2312"));
//				pw.write(contents);
//				pw.flush();
//
//			} catch (IOException ex) {
//				Logger.getLogger(WriteUrl2Txt.class.getName()).log(
//						Level.SEVERE, null, ex);
//			} finally {
//				if (pw != null) {
//					try {
//						pw.close();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//			i++;
		}
		return htmlContents;
	}
}

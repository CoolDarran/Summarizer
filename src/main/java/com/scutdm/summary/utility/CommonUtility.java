package com.scutdm.summary.utility;

import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.ByteOrderMarkDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.ParsingDetector;
import info.monitorenter.cpdetector.io.UnicodeDetector;

import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.scutdm.summary.action.SummaryResult;

/**
 * 常用工具类
 * @author danran
 * Created on 2014年5月7日
 */
public class CommonUtility {
	private static Date date;
	private static SimpleDateFormat sdf;
	private static String[] ignoreUrls = { "people.com.cn", "nbd.com.cn",
			"zjol.com.cn", "yangtse.com", "blog.ifeng.com", "xinhuanet.com",
			"chinanews.com", "qzwb.com" };
	
	/**
	 * 输出时间间隔
	 * 
	 * @param title
	 * @param startTime
	 * @param endTime
	 */
	public static void printTimeElapsed(String title, long startTime, long endTime){
		date = new Date(endTime - startTime);
		sdf = new SimpleDateFormat("HH:mm:ss:SS");
		sdf.setTimeZone(new java.util.SimpleTimeZone(0, "UTC"));
		System.out.println(title + " Time Elapsed (HH:mm:ss:SS): " + sdf.format(date));
	}

	/**
	 * 输出结果
	 * 
	 * @param sum
	 */
	public static void printTimeElapsed(SummaryResult sum) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SS");
		sdf.setTimeZone(new java.util.SimpleTimeZone(0, "UTC"));
		
		System.out.println("Read Google RSS Time Elapsed (HH:mm:ss:SS): " + sdf.format(new Date(sum.getReadHTMLTime())));
		System.out.println("Read HTML and Text Extract Time Elapsed (HH:mm:ss:SS): " + sdf.format(new Date(sum.getTextExtractTime())));
		System.out.println("Summarizer Time Elapsed (HH:mm:ss:SS): " + sdf.format(new Date(sum.getSummarizerTime())));
	}
	
	/**
	 * 判断url是否可访问
	 * 
	 * @param url
	 * @return
	 */
	public static boolean accessableUrls(String url){
		int count = 0;
		for (int i = 0; i < ignoreUrls.length; i++) {
			if (url.indexOf(ignoreUrls[i]) == -1) {
				count++;
			}
		}
		if(count == ignoreUrls.length)
			return true;
		return false;
	}
	
	/**
	 * 计算文章平均长度
	 * 
	 * @param textList
	 * @param isChinese
	 * @return
	 */
	public static int txtAvgNum(List<String> textList, boolean isChinese) {
		int count = 0;
		if(!isChinese)
			for(String text : textList){
				count+= text.split("\\s+").length;
			}
		else
			for(String text : textList){
				for(int i = 0; i < text.length(); i++){
					char tempStr = text.charAt(i);
					if(tempStr>=19968&&tempStr<=64041)
						count++;
				}
			}
		return count/textList.size();
	}
	
	/**
	 * 设置网页访问代理
	 */
	public static void setProxy(){
		// set goagent proxy
		System.setProperty("http.proxySet", "true");
		System.setProperty("http.proxyHost", "127.0.0.1");
		System.setProperty("http.proxyPort", "8087");
	}
	
	/**
	 * check the charset of a web page
	 * 
	 * @param url
	 * @return
	 */
	public static String checkCharset(URL url){
		CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();  
		detector.add(JChardetFacade.getInstance());
		detector.add(new ByteOrderMarkDetector()); 
		detector.add(new ParsingDetector(false));   
        detector.add(ASCIIDetector.getInstance());  
        detector.add(UnicodeDetector.getInstance());  
        Charset charset = null;  
        try {  
            charset = detector.detectCodepage(url);  
        } catch (Exception ex) {  
            ex.printStackTrace();  
        }  
        String charsetName = null;  
        if (charset != null) {
        	charsetName = charset.name();
        } else {  
            charsetName = "gb2312";  
        }
		return charsetName;
	}
	
	/**
	 * check whether the char is Chinese
	 * using unicode
	 * 
	 * @param c
	 * @return
	 */
	private static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
			return true;
		}
		return false;
	}
	
	/**
	 * check whether the string is Chinese
	 * 
	 * @param strName
	 * @return
	 */
	public static boolean isChinese(String strName) {
		char[] ch = strName.toCharArray();
		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (isChinese(c)) {
				return true;
			}
		}
		return false;
	}
}

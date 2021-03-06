package com.scutdm.summary.extract;

import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.ByteOrderMarkDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.ParsingDetector;
import info.monitorenter.cpdetector.io.UnicodeDetector;

import java.net.URL;
import java.nio.charset.Charset;

public class Check {
	
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

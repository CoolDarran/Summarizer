package com.scutdm.summary.utility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.scutdm.summary.action.SummaryResult;

/**
 * ������
 * @author danran
 * Created on 2014��5��7��
 */
public class Utility {
	private static Date date;
	private static SimpleDateFormat sdf;
	private static String[] ignoreUrls = { "people.com.cn", "nbd.com.cn",
			"zjol.com.cn", "yangtse.com", "blog.ifeng.com", "xinhuanet.com",
			"chinanews.com", "qzwb.com" };
	
	/**
	 * ���ʱ����
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
	 * ������
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
	 * �ж�url�Ƿ�ɷ���
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
	 * ��������ƽ������
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
	 * ������ҳ���ʴ���
	 */
	public static void setProxy(){
		// set goagent proxy
		System.setProperty("http.proxySet", "true");
		System.setProperty("http.proxyHost", "127.0.0.1");
		System.setProperty("http.proxyPort", "8087");
	}
}

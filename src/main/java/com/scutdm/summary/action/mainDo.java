package com.scutdm.summary.action;

import static java.lang.System.out;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.scutdm.summary.extract.Check;
import com.scutdm.summary.helper.SearchHelper;
import com.scutdm.summary.utility.Utility;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.sussex.nlp.jws.JWS;

/**
 * get keywords and search it
 * @author L.danran
 * Created on Apr 3, 2014
 */
public class mainDo{
	
	private static SummaryResult sum = new SummaryResult();
	
	public static void main(String args[]) throws Exception{
		
		if(args.length == 0){
			out.println("plz input search key words: [word1] [word2] [word3] ...");
			return;
		}
		/**initialization start**/
		
		File file = new File("result/");
        if (!file.exists()) {
            file.mkdirs();
        }
        JWS ws = Instance.WS;
        URL wordnetDir = new URL("file",null,"./WordNet/2.1/dict");
        Instance.setDict(new Dictionary(wordnetDir));
        IDictionary dict = Instance.getDict();
        /**initialization end**/
		
		String keyWords = "";
		int i = 1;
		for(String arg : args){
			keyWords += arg;
			if(i<args.length){
				keyWords +=  " ";
				i++;
			}
		}
		
		for(String keyWord : keyWords.split("-")){
			try {
				String fileName = keyWord;
				BufferedWriter bw = new BufferedWriter(new FileWriter(new File("result/"+ fileName +".txt")));
				out.println("Summarizer for " + keyWord + "....");
				long startTime = System.currentTimeMillis();

//				sum = SearchHelper.searchGoogle(keyWord,ws,dict);
				sum = SearchHelper.doSummarizer(keyWord,ws,dict);

				long endTime = System.currentTimeMillis();
				
				sum.setAvgNum(Utility.txtAvgNum(sum.getTextList(),Check.isChinese(keyWord)));
				Date date = new Date(endTime - startTime);
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SS");
				sdf.setTimeZone(new java.util.SimpleTimeZone(0, "UTC"));
				
				bw.write(sum.toString());
				bw.write("Time Elapsed (HH:mm:ss:SS): " + sdf.format(date));
				bw.newLine();
				bw.write("Read HTML Time Elapsed (HH:mm:ss:SS): " + sdf.format(new Date(sum.getReadHTMLTime())));
				bw.newLine();
				bw.write("Text Extract Time Elapsed (HH:mm:ss:SS): " + sdf.format(new Date(sum.getTextExtractTime())));
				bw.newLine();
				bw.write("Summarizer Time Elapsed (HH:mm:ss:SS): " + sdf.format(new Date(sum.getSummarizerTime())));
				bw.newLine();
				bw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		out.println("All Ended....");
	}	
	
}

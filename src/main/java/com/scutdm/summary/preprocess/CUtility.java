package com.scutdm.summary.preprocess;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ictclas4j.bean.SegResult;
import org.ictclas4j.segment.SegTag;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import com.scutdm.summary.doc.CWord;

import edu.buaa.edu.wordsimilarity.WordSimilarity;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * 预处理工具类
 * @author wRap
 *
 */
public class CUtility {
	
	static SegTag segTag = new SegTag(1);// 分词路径的数目
	
	/*
	 * IK分词方法，返回分词后的词语链表
	 */
	public static LinkedList<String> IKAnalysis(String str) {
		LinkedList<String> word = new LinkedList<String>();
		try {
			byte[] bt = org.ictclas4j.utility.Utility.getBytes(str);	
			InputStream ip = new ByteArrayInputStream(bt);
			Reader read = new InputStreamReader(ip);
			IKSegmenter iks = new IKSegmenter(read, true);	//分词
			Lexeme t;
			while ((t = iks.next()) != null) {	//记录分词
				word.add(t.getLexemeText());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return word;
	}
	
	/*
	 * ictclas分词
	 */
	public static String ICTCLAS(String sentence){
//		SegTag segTag = new SegTag(1);// 分词路径的数目
        SegResult segResult = segTag.split(sentence.trim());  
        String classifyContent = segResult.getFinalResult(); 
        return classifyContent;
	}
	
	/*
	 * 去除空格、换行等
	 */
	public static String replaceBlank(String str) {
		String dest = "";
		if (str!=null) {
//			Pattern p = Pattern.compile("　|\\s*|\t|\r|\n");
			Pattern p = Pattern.compile(" |\\s+|\t|\r|\n");
			Matcher m = p.matcher(str);
			dest = m.replaceAll(" ");
			p = Pattern.compile("\"|“|”|【|】");
			m = p.matcher(dest);
			dest = m.replaceAll("");
		}
		return dest;
	}
	
	 // 保留边界的split。边界归到前一个子串，置于前一个子串末尾
	 //@param input:split的原始数据字符串
	 // @param borderRegex：split的正则，分割边界正则
	 // @return
	public  static  String[] split(String input,String borderRegex){
		return split(input, borderRegex, true);
	}
	
	// 保留边界的split。边界根据borderBehind的值判断归属
	// @param input:split的原始数据字符串
	// @param borderRegex：split的正则，分割边界正则
	// @param borderBehind：边界分隔符归前一个子串，置于前一个子串末尾.true边界归前一个子串，false边界归后一个子串
	// @return
	//
	public  static  String[] split(String input,String borderRegex,boolean borderBehind){
		int index = 0;
		ArrayList<String> matchList = new ArrayList<String>();
	        
		Pattern  p = Pattern.compile(borderRegex);
		Matcher m = p.matcher(input);

		// Add segments before each match found
		while(m.find()) {
			int tempIndex = -1;
			if(borderBehind){
				tempIndex = m.end();
			}
			else{
				tempIndex = m.start();
			}
			String match = input.subSequence(index, tempIndex).toString();
			matchList.add(match);
			if(borderBehind){
				index = m.end();
			}
			else{
				index = m.start();
			}
		}
	
		// If no match was found, return this
		if (index == 0)
			return new String[] {input.toString()};
	
		// Add remaining segment
		matchList.add(input.subSequence(index, input.length()).toString());
	
		// Construct result
		int resultSize = matchList.size();
		while (resultSize > 0 && matchList.get(resultSize-1).equals(""))
			resultSize--;
		String[] result = new String[resultSize];
		return matchList.subList(0, resultSize).toArray(result);
	}
	
	/*
	 * 读取文件夹下的文件路径，返回所有文件绝对路径
	 */
	public static String[] getPath(String path){
		File file = new File(path);
		String[] paths = file.list();
		ArrayList<String> pathsList = new ArrayList<String>();
		int length = paths.length;
		for(int i=0; i<length; i++){
			if(!paths[i].contains("~") && !paths[i].contains("bak")){
				pathsList.add(path + "/" + paths[i]);
			}
		}
		String[] absPaths = new String[pathsList.size()];
		pathsList.toArray(absPaths);
		return absPaths;
	}
	
	/*
	 * 读取文件内容，参数为文件绝对路径
	 */
	public static String readFile(String path){
		File file = new File(path);
		Reader reader = null;
		StringBuilder strBuild = new StringBuilder("");
		try {
			reader = new InputStreamReader(new FileInputStream(file));
			int temp;
			while((temp = reader.read()) != -1){
				strBuild.append((char)temp);
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return strBuild.toString();
	}
	
	//标注词语词性
	public static String wordTag(MaxentTagger tagger,String sentence){
		String tagged =  tagger.tagString(sentence);
		return tagged;
	}
	
	//词语消除歧义
	public static void senseDisambiguation(CWord[] words){
        for(int i=0;i<words.length;i++){
        	String token = words[i].getLemma();
        	List<edu.buaa.edu.wordsimilarity.Word> list1 = WordSimilarity.getWordSense(token);
        	if(list1==null){
        		continue;
        	}
        	if(list1.size()==1){
        		words[i].setSense(0);
    	        words[i].setStem(token);
    	        continue;
        	}
    		int sense = 0;
    		double max = 0;
	        for(int s=0;s<list1.size();s++){		//对每个词义，计算其与前后各6个同词性词语词义相似性的和，取相似度最大的词义
	        	double sum = 0;
	        	for(int j=i-1;j>=0&&j>=i-6;j--){
	        		if(list1.get(s).getWord().equals(words[j].getLemma()))
	        			sum = sum + 1 * (1.0/Math.log(1+i-j));
	        		else 
	        			sum = sum + WordSimilarity.simWordSum(list1.get(s), words[j].getLemma()) * (1.0/Math.log(1+i-j));
	        	}
	        	for(int j=i+1;j<words.length&&j<=i+6;j++){
	        		if(list1.get(s).getWord().equals(words[j].getLemma()))
	        			sum = sum + 1 * (1.0/Math.log(1+j-i));
	        		else 
	        			sum = sum + WordSimilarity.simWordSum(list1.get(s), words[j].getLemma()) * (1.0/Math.log(1+j-i));
	        	}
	        	if(sum>max){
	        		max = sum;
	        		sense = s;
	        	}
	        }
	        words[i].setSense(sense);
	        words[i].setStem(token);
        }
        
	}

}

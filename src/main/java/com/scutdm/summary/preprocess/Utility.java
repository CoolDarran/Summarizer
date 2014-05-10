package com.scutdm.summary.preprocess;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.scutdm.summary.doc.EWord;

import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.WordnetStemmer;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.sussex.nlp.jws.JWS;

/**
 * 预处理工具类
 * @author wRap
 *
 */

public class Utility {
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
	
	//获取数据集文章正文
	public static String getText1(String doc){
		String regEx_html="<[^>]+>"; //定义HTML标签的正则表达式 
		String srcText = doc.split("<TEXT>")[1];
		srcText = srcText.split("</TEXT>")[0];
		String destText = "";
		if (srcText!=null) {
			Pattern pattern=Pattern.compile(regEx_html,Pattern.CASE_INSENSITIVE); 
	        Matcher matcher=pattern.matcher(srcText); 
	        destText=matcher.replaceAll(""); //过滤html标签 
	        pattern = Pattern.compile("\n");
			matcher = pattern.matcher(destText);
			destText = matcher.replaceAll(" "); //过滤换行符
		}
		return destText;
	}
	
	public static String getText2(String doc){
		String destText = "";
		if (doc!=null) {
	        Pattern pattern = Pattern.compile("\n");
			Matcher matcher = pattern.matcher(doc);
			destText = matcher.replaceAll(" "); //过滤换行符
			pattern = Pattern.compile("’");
			matcher = pattern.matcher(destText);
			destText = matcher.replaceAll("'"); //过滤换行符
		}
		return destText;
	}
	
	//词语消除歧义
	public static void senseDisambiguation(EWord[] words,POS pos, JWS ws,IDictionary dict){
		try {
	        dict.open();
	        for(int i=0;i<words.length;i++){
	        	String token = words[i].getLemma();
	        	IIndexWord idxWord = dict.getIndexWord(token, pos);
	        	if(idxWord==null){
	        		WordnetStemmer stemmer = new WordnetStemmer(dict);
	            	List<String> sterms = stemmer.findStems(token, pos); //获取词语词根原形
	            	if(sterms.size()==0){
//	            		System.out.println("Err:"+token+" not found");
	            		continue;
	            	}
	            	token = sterms.get(0);
	            	idxWord = dict.getIndexWord(token, pos);
	            	if(idxWord==null){
//	            		System.out.println("Err:"+token+" not found");
	            		continue;
	            	}
	            }
		        List<IWordID> wordIDs = idxWord.getWordIDs();	//根据词语获取词义id
		        String posPre = "";
        		if(pos.equals(POS.NOUN))
        			posPre = "n";
        		else
        			posPre = "v";
        		int sense = 1;
        		double max = 0;
		        for(int s=1;s<=wordIDs.size();s++){		//对每个词义，计算其与前后各6个同词性词语词义相似性的和，取相似度最大的词义
		        	double sum = 0;
		        	for(int j=i-1;j>=0&&j>=i-6;j--){
		        		EWordDistance sm= new EWordDistance(token,s,words[j].getLemma(),0,posPre, ws);
		        		sum += sm.sumScoreOfLin() * (1.0/Math.log(1+i-j));
		        	}
		        	for(int j=i+1;j<words.length&&j<=i+6;j++){
		        		EWordDistance sm= new EWordDistance(token,s,words[j].getLemma(),0,posPre, ws);
		        		sum += sm.sumScoreOfLin() * (1.0/Math.log(1+j-i));
		        	}
		        	if(sum>max){
		        		max = sum;
		        		sense = s;
		        	}
		        }
		        words[i].setSense(sense);
		        words[i].setStem(token);
	        }
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
}

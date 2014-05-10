package com.scutdm.summary.preprocess;

import java.io.IOException;
import java.util.TreeMap;

import edu.sussex.nlp.jws.JWS;
import edu.sussex.nlp.jws.Lin;

/**
 * 英文词语距离计算类
 * @author wRap
 *
 */

public class EWordDistance {
    //private static final String dir = "D:/Program Files/WordNet";
//	private static final String dir = "./WordNet";
    private JWS    ws;
	private String str1;
    private String str2;
    private int sense1;
    private int sense2;
    private String POS;
    
    public EWordDistance(String str1,int sense1,String str2,int sense2,String POS, JWS ws2){
        this.str1=str1;
        this.sense1=sense1;
        this.str2=str2;
        this.sense2=sense2;
        this.POS=POS;
        this.ws = ws2;
    }

	public String getStr1() {
		return str1;
	}


	public void setStr1(String str1) {
		this.str1 = str1;
	}


	public String getStr2() {
		return str2;
	}


	public void setStr2(String str2) {
		this.str2 = str2;
	}


	public int getSense1() {
		return sense1;
	}

	public void setSense1(int sense1) {
		this.sense1 = sense1;
	}

	public int getSense2() {
		return sense2;
	}

	public void setSense2(int sense2) {
		this.sense2 = sense2;
	}

	public String getPOS() {
		return POS;
	}

	public void setPOS(String pOS) {
		POS = pOS;
	}
	
	//计算句子短语最大相似度
	public double getSimilarity(){
        String[] strs1 = splitString(str1);
        String[] strs2 = splitString(str2);
        double sum = 0.0;
        for(String s1 : strs1){
            for(String s2: strs2){
                double sc= maxScoreOfLin(s1,s2);
                sum+= sc;
//                System.out.println("当前计算: "+s1+" VS "+s2+" 的相似度为:"+sc);
            }
        }
        double Similarity = sum /(strs1.length * strs2.length);
        sum=0;
        return Similarity;
    }
    
    private String[] splitString(String str){
        String[] ret = str.split(" ");
        return ret;
    }
    
    //词语最大相似度
    private double maxScoreOfLin(String str1,String str2){
        Lin lin = ws.getLin();
        double sc = lin.max(str1, str2, "n");
        if(sc==0){
            sc = lin.max(str1, str2, "v");
        }
        return sc;
    }
    
    //确定语义间相似度
    public double scoreOfLin(){
    	Lin lin = ws.getLin();
    	double sc = lin.lin(str1, sense1, str2, sense2, POS);
    	return sc;
    }
    
    //确定语义str1与不确定语义str2的各种语义相似度之和
    public double sumScoreOfLin(){
    	Lin lin = ws.getLin();
    	TreeMap<String,Double> sc = lin.lin(str1, sense1, str2, POS);
    	double sum=0;
    	for(String s : sc.keySet()){
    		sum += sc.get(s);
//    		System.out.println(s + "\t" + sc.get(s));
    	}
    	return sum;
    }
    
    public static void main(String args[]) throws IOException{      
//        String s1="speak";
//        String s2="say";
//        String s3="Mr";
//        String s4="Ms";
//        WordDistance sm= new WordDistance(s1,1,s2,4,"v");
//        System.out.println(sm.scoreOfLin());
//        System.out.println(sm.sumScoreOfLin());
//        WordDistance sm2= new WordDistance(s3,1,s4,1,"n");
//        System.out.println(sm2.scoreOfLin());
//        System.out.println(sm2.getSimilarity());
        
    }
}

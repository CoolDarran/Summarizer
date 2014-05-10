package com.scutdm.summary.analyz;

import com.scutdm.summary.doc.ESentence;

/**
 * 英文
 * 计算句子间距离
 * @author wRap
 *
 */
public class ESentenceDistance {
    
    //根据向量计算cosine相似度
	public double getSimilarity1(ESentence s1,ESentence s2){
		double numerator = 0;
		double denominator1 = 0;
		double denominator2 = 0;
		for(String key1:s1.concept.keySet()){
			denominator1 += Math.pow(s1.concept.get(key1), 2);
			if(s2.concept.containsKey(key1)){
				numerator += s1.concept.get(key1)*s2.concept.get(key1);
			}
		}
		denominator1 = Math.sqrt(denominator1);
		for(String key2:s2.concept.keySet()){
			denominator2 += Math.pow(s2.concept.get(key2), 2);
		}
		denominator2 = Math.sqrt(denominator2);
		double similarity = numerator/(denominator1 * denominator2);
		return similarity;
	}
	
	/*
	 * 根据编辑距离计算两句子间相似度
	 */
	public static double getSimilarity2(ESentence s1, ESentence s2){
		int ld = getEditDist(s1.getWordClusterStrings(), s2.getWordClusterStrings());
		return 1 - (double) ld / Math.max(s1.getWordClusterStrings().length, s2.getWordClusterStrings().length); 
	}
	
	/*
	 * 计算量字符串编辑距离
	 */
	public static int getEditDist(String[] s, String[] t) {
		int d[][];
		int sLen = s.length;
		int tLen = t.length;
		int si;	
		int ti;	
		String str1;
		String str2;
		int cost;
		if(sLen == 0) {
			return tLen;
		}
		if(tLen == 0) {
			return sLen;
		}
		d = new int[sLen+1][tLen+1];
		for(si=0; si<=sLen; si++) {
			d[si][0] = si;
		}
		for(ti=0; ti<=tLen; ti++) {
			d[0][ti] = ti;
		}
		for(si=1; si<=sLen; si++) {
			str1 = s[si-1];
			for(ti=1; ti<=tLen; ti++) {
				str2 = t[ti-1];
				if(str1.equals(str2)) {
					cost = 0;
				} else {
					cost = 1;
				}
				d[si][ti] = Math.min(Math.min(d[si-1][ti]+1, d[si][ti-1]+1),d[si-1][ti-1]+cost);
			}
		}
		return d[sLen][tLen];
	}
	
	//根据最长公共子序列计算相似度
	public static double getSimilarity3(ESentence s1, ESentence s2){
		int ld = comSubstring(s1.getWordClusterStrings(), s2.getWordClusterStrings());
		return ld / Math.sqrt(s1.getWordClusterStrings().length * s2.getWordClusterStrings().length); 
	}

	//动态规划实现最长公共字符串问题
	private static int comSubstring(String[] str1, String[] str2) {
		String[] a = str1;
		String[] b = str2;
		int a_length = a.length;
		int b_length = b.length;
		int[][] lcs = new int[a_length + 1][b_length + 1];
		// 初始化数组
		for (int i = 0; i <= b_length; i++) {
			for (int j = 0; j <= a_length; j++) {
				lcs[j][i] = 0;
			}
		}
		for (int i = 1; i <= a_length; i++) {
			for (int j = 1; j <= b_length; j++) {
				if (a[i - 1].equals(b[j - 1])) {
					lcs[i][j] = lcs[i - 1][j - 1] + 1;
				}
				if (!a[i - 1].equals(b[j - 1])) {
					lcs[i][j] = lcs[i][j - 1] > lcs[i - 1][j] ? lcs[i][j - 1]
							: lcs[i - 1][j];
				}
			}
		}
		// 输出数组结果进行观察
//		for (int i = 0; i <= a_length; i++) {
//			for (int j = 0; j <= b_length; j++) {
//				System.out.print(lcs[i][j]+",");
//			}
//			System.out.println("");
//		}
		// 由数组构造最小公共字符串
		int max_length = lcs[a_length][b_length];
		String[] comStr = new String[max_length];
		int i =a_length, j =b_length;
		while(max_length>0){
			if(lcs[i][j]!=lcs[i-1][j-1]){
				if(lcs[i-1][j]==lcs[i][j-1]){//两字符相等，为公共字符
					comStr[max_length-1]=a[i-1];
					max_length--;
					i--;j--;
				}else{//取两者中较长者作为A和B的最长公共子序列
					if(lcs[i-1][j]>lcs[i][j-1]){
						i--;
					}else{
						j--;
					}
				}
			}else{
				i--;j--;
			}
		}
//		System.out.print("最长公共字符串是：");
//		for(String str:comStr)
//			System.out.print(str);
//		System.out.println("\n" + comStr.length);
		return comStr.length;
	}
	
	//直接根据词语语义计算相似度
//	public double getSimilarity4(Sentence s1,Sentence s2){
//		Lin lin = ws.getLin();
//		double sum = 0;
//		List<Word> noun1 = s1.getNounWords();
//		List<Word> noun2 = s2.getNounWords();
//		List<Word> verb1 = s1.getVerbWords();
//		List<Word> verb2 = s2.getVerbWords();
//		int size1 = noun1.size()+verb1.size();
//		int size2 = noun2.size()+verb2.size();
//		for(Word word1:noun1){
//			if(word1.getSense()==0){
//				--size1;
//				continue;
//			}
//			for(Word word2:noun2){
//				if(word2.getSense()==0)
//					continue;
////				System.out.println(word1.getStem()+" "+ word1.getSense()+" "+ word2.getStem()+" "+ word2.getSense());
//				sum += lin.lin(word1.getStem(), word1.getSense(), word2.getStem(), word2.getSense(), "n");
//			}
//		}
//		for(Word word1:verb1){
//			if(word1.getSense()==0){
//				--size1;
//				continue;
//			}
//			for(Word word2:verb2){
//				if(word2.getSense()==0)
//					continue;
////				System.out.println(word1.getStem()+" "+ word1.getSense()+" "+ word2.getStem()+" "+ word2.getSense());
//				sum += lin.lin(word1.getStem(), word1.getSense(), word2.getStem(), word2.getSense(), "v");
//			}
//		}
//		for(Word word:noun2){
//			if(word.getSense()==0){
//				--size2;
//			}
//		}
//		for(Word word:verb2){
//			if(word.getSense()==0){
//				--size2;
//			}
//		}
//		double similarity = sum/(size1*size2);
//		return similarity;
//	}
}

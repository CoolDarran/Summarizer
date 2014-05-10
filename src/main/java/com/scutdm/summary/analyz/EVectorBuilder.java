package com.scutdm.summary.analyz;

import java.util.LinkedList;
import java.util.List;

import com.scutdm.summary.doc.ESentence;
import com.scutdm.summary.doc.EWord;

/**
 * 根据词语聚类结果，对句子建立空间向量模型及语义概念序列
 * @author wRap
 *
 */
public class EVectorBuilder {
	public void sentVecBuilder(ESentence sentence){
		//处理名词
		for(EWord word:sentence.getNounWords()){
			if(!word.getClusterID().equals("")){
				String key = "n"+word.getClusterID();
				if(sentence.concept.containsKey(key)){
					int value = sentence.concept.get(key).intValue() + 1;
					sentence.concept.put(key, value);
				}
				else {
					sentence.concept.put(key, 1);
				}
			}
		}
		//处理动词
		for(EWord word:sentence.getVerbWords()){
			if(!word.getClusterID().equals("")){
				String key = "v"+word.getClusterID();
				if(sentence.concept.containsKey(key)){
					int value = sentence.concept.get(key).intValue() + 1;
					sentence.concept.put(key, value);
				}
				else {
					sentence.concept.put(key, 1);
				}
			}
		}
		
		//生成语义概念序列
		List<String> clusterStrings = new LinkedList<String>();
		for(EWord word:sentence.getWords()){
			if(!word.getClusterID().equals("")){
				if(word.getPOS().startsWith("N"))
					clusterStrings.add("n" + word.getClusterID());
				else 
					clusterStrings.add("v" + word.getClusterID());
			}
		}
		String[] wordClusterStrings = new String[clusterStrings.size()];
		wordClusterStrings = clusterStrings.toArray(wordClusterStrings);
		sentence.setWordClusterStrings(wordClusterStrings);
	}

}

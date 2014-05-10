package com.scutdm.summary.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.scutdm.summary.doc.EWord;
import com.scutdm.summary.preprocess.EWordDistance;

import edu.sussex.nlp.jws.JWS;

/**
 * 英文
 * 词语DBSCAN聚类分析类
 * @author wRap
 *
 */
public class EWordDBSCAN {
	private double epsilon=0.6;//ε半径
	private int minPts=3;//密度阈值
	private String POS="";
	private List<EWord> wordList=new ArrayList<EWord>();//存储原始样本点
	private List<List<EWord>> resultList=new ArrayList<List<EWord>>();//存储最后的聚类结果
	private HashMap<Long,HashMap<Long,Double>> scoreOfLin = new HashMap<Long, HashMap<Long, Double>>();
	private HashMap<Long,List<EWord>> scoresOfLin = new HashMap<Long, List<EWord>>();
	
	public void Cluster(List<EWord> words,double epsilon,int minPts,String POS, JWS ws){
		wordList=new ArrayList<EWord>();
		resultList=new ArrayList<List<EWord>>();
		setEpsilon(epsilon);
		setMinPts(minPts);
		setPOS(POS);
		wordList = words;
		System.out.println("\nStart to clustering word...");
//		long beginTime = System.currentTimeMillis();
		for(int index=0;index<wordList.size();++index){
  			if(index%100==0)
  				System.out.print(index + " ");
			List<EWord> tmpLst=new ArrayList<EWord>();
			EWord p=wordList.get(index);
			if(p.isClassed()||p.getSense()==0)
				continue;
			tmpLst=isKeyPoint(wordList, p, epsilon, minPts,ws);
//			tmpLst=isKeyPoint2(wordList.subList(index, wordList.size()), p, epsilon, minPts,ws);
			if(tmpLst!=null){
				resultList.add(tmpLst);
			}
		}
		/*
		 * 簇的合并
		 */
		System.out.println("\nStart to merge clusters...");
		int length=resultList.size();
		for(int i=0;i<length;++i){
			for(int j=0;j<length;++j){
				if(i!=j){
					if(mergeList(resultList.get(i), resultList.get(j))){
						resultList.get(j).clear();
					}
				}
			}
		}
		/*
		 * 删除空的簇
		 */
		System.out.println("Start to delete null clusters...");
		Iterator<List<EWord>> it = resultList.iterator();
		while(it.hasNext()){
			if(it.next().size()==0)
				it.remove();
		}
//		long runningTime = System.currentTimeMillis() - beginTime;
		
		Iterator<List<EWord>> iterator = resultList.iterator();
		int index = 1;
		while(iterator.hasNext()){
			Iterator<EWord> iterator2 = iterator.next().iterator();
//			System.out.println("-----第"+index+"个聚类-----");
			while(iterator2.hasNext()){
				EWord word = iterator2.next();
//				System.out.print(word.getLemma() + " ");
				word.setClusterID(String.valueOf(index));
			}
//			System.out.println();
			index++;
		}
//		System.out.println("簇个数：" + resultList.size());
//		System.out.println("用时：" + runningTime + "毫秒");
	}
	
	//检测p点是不是核心点，tmpLst存储核心点的直达点
	private List<EWord> isKeyPoint(List<EWord> lst,EWord p,double e,int minp, JWS ws){
		int count=0;
		List<EWord> tmpLst=new ArrayList<EWord>();
		for(Iterator<EWord> it=lst.iterator();it.hasNext();){
			EWord q=it.next();
			if(q.getSense()==0)
				continue;
			EWordDistance distance= new EWordDistance(p.getStem(),p.getSense(),q.getStem(),q.getSense(),POS,ws);
			if(distance.scoreOfLin()>=e){
				++count;
				if(!tmpLst.contains(q)){
					tmpLst.add(q);
					}
				}
			}
		if(count>=minp){
			p.setKey(true);
			return tmpLst;
			}
		return null;
	}
	
	// 检测p点是不是核心点，tmpLst存储核心点的直达点
	private List<EWord> isKeyPoint1(List<EWord> lst, EWord p, double e, int minp, JWS ws) {
		int unCalculate = 0;
		int count = 0;
		List<EWord> tmpLst = new ArrayList<EWord>();
		for (Iterator<EWord> it = lst.iterator(); it.hasNext();) {
			EWord q = it.next();
			if (q.getSense() == 0)
				continue;
			if (scoreOfLin.containsKey(p.getStoreID())
					&& scoreOfLin.get(p.getStoreID()).containsKey(q.getStoreID())) {
				++count;
				++unCalculate;
				if(!tmpLst.contains(q))
					tmpLst.add(q);
			}else{
				EWordDistance distance = new EWordDistance(p.getStem(), p.getSense(), q.getStem(), q.getSense(), POS, ws);
				double score = distance.scoreOfLin();
				if (score >= e) {
					++count;
					if (!tmpLst.contains(q)) {
						tmpLst.add(q);
					}
					
					// 存储计算过的分数
					if(scoreOfLin.containsKey(p.getStoreID())){
						scoreOfLin.get(p.getStoreID()).put(q.getStoreID(), score);
					}else{
						HashMap<Long, Double> temp = new HashMap<Long, Double>();
						temp.put(q.getStoreID(), score);
						scoreOfLin.put(p.getStoreID(), temp);
					}
					if(scoreOfLin.containsKey(q.getStoreID())){
						scoreOfLin.get(q.getStoreID()).put(p.getStoreID(), score);
					}else{
						HashMap<Long, Double> temp = new HashMap<Long, Double>();
						temp.put(p.getStoreID(), score);
						scoreOfLin.put(q.getStoreID(), temp);
					}
				}
			}
		}
		System.out.println("Uncalculate for "+ p.getLemma() +": " + unCalculate);
		if (count >= minp) {
			p.setKey(true);
			return tmpLst;
		}
		return null;
	}
	
	// 检测p点是不是核心点，tmpLst存储核心点的直达点
	private List<EWord> isKeyPoint2(List<EWord> lst, EWord p, double e, int minp, JWS ws) {
		int count = 0;
		List<EWord> tmpLst = new ArrayList<EWord>();
		for (Iterator<EWord> it = lst.iterator(); it.hasNext();) {
			EWord q = it.next();
			if (q.getSense() == 0)
				continue;
			EWordDistance distance = new EWordDistance(p.getStem(), p.getSense(), q.getStem(), q.getSense(), POS, ws);
			if (distance.scoreOfLin() >= e) {
				++count;
				if (!tmpLst.contains(q)) {
					tmpLst.add(q);
				}
				if(scoresOfLin.containsKey(p.getStoreID()))
					scoresOfLin.get(p.getStoreID()).add(q);
				else{
					List<EWord> tmp = new ArrayList<EWord>();
					tmp.add(q);
					scoresOfLin.put(p.getStoreID(), tmp);
				}
				if(scoresOfLin.containsKey(q.getStoreID()))
					scoresOfLin.get(q.getStoreID()).add(p);
				else{
					List<EWord> tmp = new ArrayList<EWord>();
					tmp.add(p);
					scoresOfLin.put(q.getStoreID(), tmp);
				}
			}
		}
		
		tmpLst.addAll(scoresOfLin.containsKey(p.getStoreID()) ? scoresOfLin.get(p.getStoreID()) : null);
		count += (scoresOfLin.containsKey(p.getStoreID()) ? scoresOfLin.get(p.getStoreID()).size() : 0);
		
		
		System.out.println("Uncalculate for " + p.getLemma() + ": " + tmpLst.size());
		if (count >= minp) {
			p.setKey(true);
			return tmpLst;
		}
		return null;
	}
		
	//合并两个链表，前提是b中的核心点包含在a中
	private boolean mergeList(List<EWord> a,List<EWord> b){
		boolean merge=false;
		if(a==null || b==null){
			return false;
			}
		for(int index=0;index<b.size();++index){
			EWord p=b.get(index);
			if(p.isKey() && a.contains(p)){
				merge=true;
				break;
				}
			}
		if(merge){
			for(int index=0;index<b.size();++index){
				if(!a.contains(b.get(index))){
					a.add(b.get(index));
					}
				}
			}
		return merge;
	}

	public double getEpsilon() {
		return epsilon;
	}

	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}

	public int getMinPts() {
		return minPts;
	}

	public void setMinPts(int minPts) {
		this.minPts = minPts;
	}

	public String getPOS() {
		return POS;
	}

	public void setPOS(String POS) {
		this.POS = POS;
	}
	
}

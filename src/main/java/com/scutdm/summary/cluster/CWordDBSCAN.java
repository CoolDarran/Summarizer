package com.scutdm.summary.cluster;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.buaa.edu.wordsimilarity.*;
import cilin.Cilin;

import com.scutdm.summary.doc.CWord;

/**
 * 中文 词语DBSCAN聚类分析类
 * 
 * @author wRap
 * 
 */
public class CWordDBSCAN {
	private double epsilon = 0.9;// ε半径
	private int minPts = 3;// 密度阈值
	private String POS = "";
	private List<CWord> wordList = new ArrayList<CWord>();// 存储原始样本点
	private List<List<CWord>> resultList = new ArrayList<List<CWord>>();// 存储最后的聚类结果

	public void Cluster(List<CWord> words, double epsilon, int minPts,
			String POS) {
		wordList = new ArrayList<CWord>();
		resultList = new ArrayList<List<CWord>>();
		setEpsilon(epsilon);
		setMinPts(minPts);
		setPOS(POS);
		wordList = words;
		System.out.println("\nStart to clustering word...");
		// long beginTime = System.currentTimeMillis();
		for (int index = 0; index < wordList.size(); ++index) {
			if (index % 100 == 0)
				System.out.print(index + " ");
			List<CWord> tmpLst = new ArrayList<CWord>();
			CWord p = wordList.get(index);
			if (p.isClassed() || p.getSense() == -1
					|| p.getLemma().length() < 2)
				// if(p.isClassed())
				continue;
			tmpLst = isKeyPoint(wordList, p, epsilon, minPts);
//			tmpLst = isKeyPointCILIN(wordList, p, epsilon, minPts);
			if (tmpLst != null) {
				resultList.add(tmpLst);
			}
		}
		/*
		 * 簇的合并
		 */
		System.out.println("\nStart to merge clusters...");
		int length = resultList.size();
		for (int i = 0; i < length; ++i) {
			for (int j = 0; j < length; ++j) {
				if (i != j) {
					if (mergeList(resultList.get(i), resultList.get(j))) {
						resultList.get(j).clear();
					}
				}
			}
		}
		/*
		 * 删除空的簇
		 */
		System.out.println("Start to delete null clusters...");
		Iterator<List<CWord>> it = resultList.iterator();
		while (it.hasNext()) {
			if (it.next().size() == 0)
				it.remove();
		}
		// long runningTime = System.currentTimeMillis() - beginTime;

		Iterator<List<CWord>> iterator = resultList.iterator();
		int index = 1;
		while (iterator.hasNext()) {
			Iterator<CWord> iterator2 = iterator.next().iterator();
			// System.out.println("-----第"+index+"个聚类-----");
			while (iterator2.hasNext()) {
				CWord word = iterator2.next();
				// System.out.print(word.getLemma() + " ");
				word.setClusterID(String.valueOf(index));
			}
			// System.out.println();
			index++;
		}
		// System.out.println("簇个数：" + resultList.size());
		// System.out.println("用时：" + runningTime + "毫秒");
	}

	// 检测p点是不是核心点，tmpLst存储核心点的直达点
	private List<CWord> isKeyPoint(List<CWord> lst, CWord p, double e, int minp) {
		int count = 0;
		List<CWord> tmpLst = new ArrayList<CWord>();
		for (Iterator<CWord> it = lst.iterator(); it.hasNext();) {
			CWord q = it.next();
			if (q.getSense() == -1 || q.getLemma().length() < 2)
				continue;
			// System.out.println(p.getLemma() + "," + q.getLemma());
			if (p.getLemma().equals(q.getLemma())
					|| WordSimilarity.simWord(p.getLemma(), p.getSense(),
							q.getLemma(), q.getSense()) >= e) {
				++count;
				if (!tmpLst.contains(q)) {
					tmpLst.add(q);
				}
			}
		}
		if (count >= minp) {
			p.setKey(true);
			return tmpLst;
		}
		return null;
	}

	// 检测p点是不是核心点，tmpLst存储核心点的直达点
	// 使用同义词词林
	private List<CWord> isKeyPointCILIN(List<CWord> lst, CWord p, double e, int minp) {
		int count = 0;
		List<CWord> tmpLst = new ArrayList<CWord>();
		for (Iterator<CWord> it = lst.iterator(); it.hasNext();) {
			CWord q = it.next();
			if (q.getSense() == -1 || q.getLemma().length() < 2)
				continue;
			// System.out.println(p.getLemma() + "," + q.getLemma());
			if (p.getLemma().equals(q.getLemma())
					|| Cilin.getInstance().getSimilarity(p.getLemma(), q.getLemma()) >= e){
				++count;
				if (!tmpLst.contains(q)) {
					tmpLst.add(q);
				}
			}
		}
		if (count >= minp) {
			p.setKey(true);
			return tmpLst;
		}
		return null;
	}

	// 合并两个链表，前提是b中的核心点包含在a中
	private boolean mergeList(List<CWord> a, List<CWord> b) {
		boolean merge = false;
		if (a == null || b == null) {
			return false;
		}
		for (int index = 0; index < b.size(); ++index) {
			CWord p = b.get(index);
			if (p.isKey() && a.contains(p)) {
				merge = true;
				break;
			}
		}
		if (merge) {
			for (int index = 0; index < b.size(); ++index) {
				if (!a.contains(b.get(index))) {
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

package com.scutdm.summary.extract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * <p>
 * ������ʱ���ڳ�ȡ�����ࣨ���š����͵ȣ���ҳ�����ġ�
 * ������<b>�����п�ֲ�����</b>�ķ�����Ϊ����ͨ����û������ض���վ��д����
 * </p>
 * @author Chen Xin(xchen@ir.hit.edu.cn)
 * Created on 2009-1-11
 * Updated on 2010-08-09
 */
public class TextExtract {
	
	private List<String> lines;
	private final static int blocksWidth=3;
	private int threshold;
	private String html;
	private boolean flag;
	private int start;
	private int end;
	private StringBuilder text;
	private ArrayList<Integer> indexDistribution;
	
	public TextExtract() {
		lines = new ArrayList<String>();
		indexDistribution = new ArrayList<Integer>();
		text = new StringBuilder();
		flag = false;
		/* ������ȡ����ҳ�����������ɿ�����ű���δ�޳�ʱ��ֻҪ�������ֵ���ɡ�*/
		/* ��ֵ����׼ȷ���������ٻ����½���ֵ��С��������󣬵����Ա�֤�鵽ֻ��һ�仰������ */
		threshold	= -1;   
	}
	

	/**
	 * ��ȡ��ҳ���ģ����жϸ���ҳ�Ƿ���Ŀ¼�͡�����֪����Ŀ϶��ǿ��Գ�ȡ���ĵ���������ҳ��
	 * 
	 * @param _html ��ҳHTML�ַ���
	 * 
	 * @return ��ҳ����string
	 */
	public String parse(String _html) {
		return parse(_html, false);
	}
	
	/**
	 * �жϴ���HTML��������������ҳ�����ȡ���ģ��������<b>"unkown"</b>��
	 * 
	 * @param _html ��ҳHTML�ַ���
	 * @param _flag true�����������ж�, ʡ�Դ˲�����Ĭ��Ϊfalse
	 * 
	 * @return ��ҳ����string
	 */
	public String parse(String _html, boolean _flag) {
		flag = _flag;
		html = _html;
		html = preProcess(html);
//		System.out.println(html);
		return getText();
	}
	
	private static int FREQUENT_URL = 30;
	
	private static Pattern links = Pattern.compile("<[aA]\\s+[Hh][Rr][Ee][Ff]=[\"|\']?([^>\"\' ]+)[\"|\']?\\s*[^>]*>([^>]+)</a>(\\s*.{0,"+FREQUENT_URL+"}\\s*<a\\s+href=[\"|\']?([^>\"\' ]+)[\"|\']?\\s*[^>]*>([^>]+)</[aA]>){2,100}", Pattern.DOTALL);
	
	private static String preProcess(String source) {
		
		source = source.replaceAll("(?is)<!DOCTYPE.*?>", "");
		source = source.replaceAll("(?is)<!--.*?-->", "");				// remove html comment
		source = source.replaceAll("(?is)<script.*?>.*?</script>", ""); // remove javascript
		source = source.replaceAll("(?is)<style.*?>.*?</style>", "");   // remove css
		source = source.replaceAll("&.{2,5};|&#.{2,5};", " ");			// remove special char
		
		//�޳�������Ƭ�ĳ������ı�����Ϊ�ǣ�����������,�����Ӷ����span��
		source = source.replaceAll("<[sS][pP][aA][nN].*?>", "");
		source = source.replaceAll("</[sS][pP][aA][nN]>", "");

		int len = source.length();
		while ((source = links.matcher(source).replaceAll("")).length() != len)
		{
			len = source.length();
		}
			;//continue;
		
		//source = links.matcher(source).replaceAll("");
		
		//��ֹhtml����<>�а������ںŵ��ж�
		source = source.replaceAll("<[^>'\"]*['\"].*['\"].*?>", "");

		source = source.replaceAll("<.*?>", "");
		source = source.replaceAll("<.*?>", "");
		source = source.replaceAll("\r\n", "\n");

		return source;
	
	}
	
	private String getText() {
		lines = Arrays.asList(html.split("\n"));
		indexDistribution.clear();
		
		int empty = 0;//���е�����
		for (int i = 0; i < lines.size() - blocksWidth; i++) {
			
			if (lines.get(i).length() == 0)
			{
				empty++;
			}
			
			int wordsNum = 0;
			for (int j = i; j < i + blocksWidth; j++) { 
				lines.set(j, lines.get(j).replaceAll("\\s+", ""));
				wordsNum += lines.get(j).length();
			}
			indexDistribution.add(wordsNum);
			//System.out.println(wordsNum);
		}
		int sum = 0;

		for (int i=0; i< indexDistribution.size(); i++)
		{
			sum += indexDistribution.get(i);
		}
		
		threshold = Math.min(100, (sum/indexDistribution.size())<<(empty/(lines.size()-empty)>>>1));
		threshold = Math.max(50, threshold);
		
		start = -1; end = -1;
		boolean boolstart = false, boolend = false;
		boolean firstMatch = true;//ǰ��ı���������Ƚ�С��Ӧ�ü�С����ƥ�����ֵ
		text.setLength(0);
		
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < indexDistribution.size() - 1; i++) {
			
			if(firstMatch && ! boolstart)
			{
				if (indexDistribution.get(i) > (threshold/2) && ! boolstart) {
					if (indexDistribution.get(i+1).intValue() != 0 
						|| indexDistribution.get(i+2).intValue() != 0) {
						firstMatch = false;
						boolstart = true;
						start = i;
						continue;
					}
				}
				
			}
			if (indexDistribution.get(i) > threshold && ! boolstart) {
				if (indexDistribution.get(i+1).intValue() != 0 
					|| indexDistribution.get(i+2).intValue() != 0
					|| indexDistribution.get(i+3).intValue() != 0) {
					boolstart = true;
					start = i;
					continue;
				}
			}
			if (boolstart) {
				if (indexDistribution.get(i).intValue() == 0 
					|| indexDistribution.get(i+1).intValue() == 0) {
					end = i;
					boolend = true;
				}
			}
		
			if (boolend) {
				buffer.setLength(0);
				//System.out.println(start+1 + "\t\t" + end+1);
				for (int ii = start; ii <= end; ii++) {
					if (lines.get(ii).length() < 5) continue;
					buffer.append(lines.get(ii) + "\n");
				}
				String str = buffer.toString();
				//System.out.println(str);
				if (str.contains("Copyright")  || str.contains("��Ȩ����") ) continue; 
				text.append(str);
				boolstart = boolend = false;
			}
		}
		
		if (start > end)
		{
			buffer.setLength(0);
			int size_1 = lines.size()-1;
			for (int ii = start; ii <= size_1; ii++) {
				if (lines.get(ii).length() < 5) continue;
				buffer.append(lines.get(ii) + "\n");
			}
			String str = buffer.toString();
			//System.out.println(str);
			if ((!str.contains("Copyright"))  || (!str.contains("��Ȩ����")) ) 
			{	
				text.append(str);
			}
		}
		
		return text.toString();
	}
	
	public static void main(String[] args)
	{
		System.out.println("===============");
		String s = "<img  class='fit-image' onload='javascript:if(this.width>498)this.width=498;' />hello";
		//source = source.replaceAll("<[^'\"]*['\"].*['\"].*?>", "");
		System.out.println(TextExtract.preProcess(s));
	}
}
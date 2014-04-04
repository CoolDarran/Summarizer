/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scutdm.summary.crawler.core;

import org.apache.log4j.Logger;

import com.scutdm.summary.crawler.extract.TextExtract;
import com.scutdm.summary.crawler.io.SeedFileMgt;
import com.scutdm.summary.crawler.io.URLManageFactory;
import com.scutdm.summary.crawler.io.WriteUrl2Txt;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.ParseData;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 *
 * @author Administrator
 */
public class Crawl2File extends WebCrawler {

    public TextExtract textExtact = new TextExtract();
    private WriteUrl2Txt writer = new WriteUrl2Txt();
    private SeedFileMgt fileMgt;
    public Crawl2File() throws ClassNotFoundException{
        super();
        fileMgt = URLManageFactory.getURLMangeObj();
    }
    /*
     * 判断一个页面是否要被爬取
     */
    @Override
    public boolean shouldVisit(WebURL url) {
        try {
            return fileMgt.shouldFetch(url.getURL());
        } catch (Exception ex) {
           Logger.getRootLogger().error(ex);
           return false;
        }
    }
    /*
     * 一个页面被爬取后的处理方法
     */

    @Override
    public void visit(Page page) {
        //super.visit(page);

        ParseData parseData = page.getParseData();
        if (parseData instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) parseData;
            //实现正文抽取,完成写入文件
            String parseText = textExtact.parse(htmlParseData.getHtml());
            System.out.println(parseText);
            if (parseText != null && !parseText.equals("")) {
                writer.write(page.getWebURL().getURL(), parseText); //将逻辑给到了IO层！！！
            }
        }



    }

    
}

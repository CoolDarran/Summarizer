/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scutdm.summary.crawler.io;

import org.apache.log4j.Logger;

import com.scutdm.summary.crawler.core.CrawlProperties;

/**
 *
 * @author Administrator
 */
public class URLManageFactory {
    
    public static SeedFileMgt getURLMangeObj() {
        Class fileMgtClass;
        try {
            if (CrawlProperties.contains("crawl.io.url.mgt.obj")) {
                
                fileMgtClass = Class.forName(CrawlProperties.getProperty("crawl.io.url.mgt.obj"));
                
                
            } else {
                fileMgtClass = Class.forName("com.scutdm.summary.crawler.io.URlSeedFileMgt");
            }
            return (SeedFileMgt) fileMgtClass.getConstructor().newInstance();
        } catch (Exception ex) {
            Logger.getRootLogger().error("error with " + ex.getMessage());
            return null;
        }
    }
}

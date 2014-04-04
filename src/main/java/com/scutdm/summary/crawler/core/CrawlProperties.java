/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scutdm.summary.crawler.core;

import java.io.*;
import java.net.URLDecoder;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 读取crawl.properties
 * @author Administrator
 */
public class CrawlProperties {
    private static final String PROPERTY_FILE = "crawl.properties";
    private static Properties properties;
    static {
        String propertyFilePath;
        
        
        properties  = new Properties();
        
        try {
            
            propertyFilePath = CrawlProperties.class.getProtectionDomain().getCodeSource().getLocation().getPath() + PROPERTY_FILE;
            propertyFilePath = URLDecoder.decode(propertyFilePath, "UTF-8");
            System.out.println(propertyFilePath);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(propertyFilePath));
            properties.load(bis);
           
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CrawlProperties.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
                Logger.getLogger(CrawlProperties.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    public static String getProperty(String key){
        
        
        return properties.getProperty(key);
    }
    /*
     * 查询配置文件是否含有key的配置
     */
    public static boolean contains(String key){
        return properties.contains(key);
    }
    public static void main(String args[]){
       System.out.println(CrawlProperties.getProperty("crawl.storage.path"));
    }
    
}

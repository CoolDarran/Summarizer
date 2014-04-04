/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scutdm.summary.crawler.io;

import java.util.List;

/**
 *
 * @author Administrator
 */
public interface SeedFileMgt {
    public static final int URL_NOT_CORRECT = 1;
    public static final int URL_EXIT = 2;
    /**
     * 获取相应下标url
     * @param index
     * @return 
     */
    public String getUrl(int index) throws Exception;
    /**
     * 添加url
     * @param url 
     */
    public  int addUrl(String url) throws Exception;
    public  int addUrl(int index,String url) throws Exception;
    /**
     * 修改url
     */
    public int update(String url) throws Exception;
    /**
     * 删除url
     */
    public int del(String url) throws Exception;
    public int del(int index) throws Exception;
    /**
     * 写出url
     */
    public int writeUrls(List<String> url) throws Exception;
    /**
     * 获取url列表的数量
     * @return 
     */
    public int getSize() throws Exception;
    /**
     * 判断是否存在相应url
     * @return 
     */
    public boolean hasUrl(String url) throws Exception;
    /**
     * 判断url格式
     * @param url
     * @return 
     */
    public boolean isUrl(String url) throws Exception;
    /**
     * 
     * @param url
     * @return 
     */
     public  boolean shouldFetch(String url) throws Exception;
}

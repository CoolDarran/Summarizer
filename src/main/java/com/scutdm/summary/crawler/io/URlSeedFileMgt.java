/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scutdm.summary.crawler.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.scutdm.summary.crawler.core.CrawlProperties;

/**
 *
 * @author Administrator
 */
public class URlSeedFileMgt implements SeedFileMgt {

    private static int size = 0;
    private static List<String> urls = new ArrayList<String>();
    private static File seedFile = null;

    static {
        //从文件读取url
        String seedPath = "urls.txt";
        BufferedReader reader = null;
        if (CrawlProperties.contains("crawl.url.seeds")) {
            seedPath = CrawlProperties.getProperty("crawl.url.seeds");
        }
        try {
            //System.out.println(URlSeedFileMgt.class.getResource("").toURI().getPath()+seedPath);
            seedFile = new File(URlSeedFileMgt.class.getResource("").toURI().getPath()+seedPath);
        } catch (URISyntaxException ex) {
           Logger.getRootLogger().error(ex);
        }
        try {
            seedFile.createNewFile();
        } catch (IOException ex) {
           Logger.getRootLogger().error(ex);
        }

        try {
            reader = new BufferedReader(new FileReader(seedFile));

            String url = null;
            while ((url = reader.readLine()) != null) {
                urls.add(url);
                size++;
            }

        } catch (FileNotFoundException ex) {
            Logger.getRootLogger().error(ex);
        } catch (IOException ex) {
            Logger.getRootLogger().error(ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                   Logger.getRootLogger().error(ex);
                }
            }
        }
    }

    @Override
    public int update(String url) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int del(String url) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public synchronized int writeUrls(List<String> url) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getUrl(int index) {
        if (index < size) {
            return urls.get(index);
        } else {
            return null;
        }
    }

    @Override
    public synchronized int addUrl(String url) throws IOException {

        return addUrl(urls.size(), url);
    }

    @Override
    public synchronized int addUrl(int index, String url) throws IOException {

//        PrintWriter writer = new PrintWriter(new FileWriter(seedFile,true));

        if (!isUrl(url)) {
            return URL_NOT_CORRECT;
        }
        if (hasUrl(url)) {
            return URL_EXIT;
        }
        //url正确的话开始添加url
        //写入文件
        urls.add(index, url);
        size++;
        int result = synToFile();
        //加入列表
        if (result == -1) {
            urls.remove(index);
            size--;
        }
        //displayList();
        return result;
    }

    @Override
    public synchronized int del(int index) throws IOException {
        String url = urls.get(index);
        urls.remove(index);
        size--;
        //数据同步回文件
        int result = synToFile();
        if(result !=0){
            urls.add(index, url);
            size++;
        }
        //displayList();
        return result;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public boolean hasUrl(String url) {
        for (Iterator<String> itr = urls.iterator(); itr.hasNext();) {
            if (itr.next().equals(url)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldFetch(String url) {

        for (Iterator<String> itr = urls.iterator(); itr.hasNext();) {
            String nextUrl = itr.next();
            if (url.equals(nextUrl) || url.startsWith(nextUrl)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将结果同步回文件
     */
    public synchronized int synToFile() throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(seedFile));
        String url = null;
        for (Iterator<String> itr = urls.iterator(); itr.hasNext();) {
            url = itr.next();
            System.out.println("writing:" + url);
            writer.println(url);
        }
        writer.flush();
        writer.close();
        return 0;
    }

    @Override
    public boolean isUrl(String url) {
        if (url == null || url.length() == 0) {
            return false;
        }
//
//        String regEx1 = "^(http|https|ftp)\\://([a-zA-Z0-9\\.\\-]+(\\:[a-zA-"
//                + "Z0-9\\.&%\\$\\-]+)*@)?((25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{"
//                + "2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}"
//                + "[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|"
//                + "[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-"
//                + "4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])|([a-zA-Z0"
//                + "-9\\-]+\\.)*[a-zA-Z0-9\\-]+\\.[a-zA-Z]{2,4})(\\:[0-9]+)?(/"
//                + "[a-zA-Z0-9\\.\\,\\?\\'\\\\/\\+&%\\$\\=~_\\-@]*)*$";
        String regEx = CrawlProperties.getProperty("crawl.url.reg");
//        System.out.println(regEx.equals(regEx1));
        Pattern p = Pattern.compile(regEx);
        Matcher matcher = p.matcher(url);
        //System.out.println(matcher.matches());
        return matcher.matches();
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scutdm.summary.extract;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 将url爬取的结果，写入到txt文件
 *
 * @author Administrator
 */
public class WriteUrl2Txt {

    public static final int UNSUPPORT_TYPE = 1;
    public static final int FILE_NOT_EXIT = 2;
    public static String resDir;

    static {      
        resDir = "text_result/";
        
        File file = new File(resDir);
        if (!file.exists()) {
            file.mkdirs();
        }
    }
    
    //把url正文提取结果写入文本文件

    public void write(String fileName, String urlResult) {

        File file = new File(resDir + fileName + ".txt");
        //file.deleteOnExit();
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            pw.write(urlResult);
            pw.flush();

        } catch (IOException ex) {
            Logger.getLogger(WriteUrl2Txt.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    public String generateFileName(String url) throws NoSuchAlgorithmException {
        MessageDigest md = null;
        String generatedName = null;
        byte[] urlBytes = url.getBytes();

        md = MessageDigest.getInstance("MD5");

        generatedName = md.digest(urlBytes).toString();


        return generatedName;
    }

    public int readFromFile(String path) {
        if (!path.endsWith(".txt")) {
            //只支持txt文件的读写
            return UNSUPPORT_TYPE;
        }
        File file = new File(path);

        if (!file.exists()) {
            //不存在此文件
            return FILE_NOT_EXIT;
        }
        //逐行读进url
        return 0;

    }

    public static void main(String[] args) {
        //测试是否生成唯一码.
        String[] urls = {"http://www.sina.com.cn/", "http://www.sina.com.cn/abc.html", "http://www.sina.com.cn/abc2.html"};
        WriteUrl2Txt writer = new WriteUrl2Txt();
        for (String url : urls) {
            System.out.print("for " + url + ":   ");
            try {
                System.out.println(writer.generateFileName(url));
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(WriteUrl2Txt.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

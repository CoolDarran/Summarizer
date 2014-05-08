/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scutdm.summary.extract;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ��url��ȡ�Ľ����д�뵽txt�ļ�
 *
 * @author Administrator
 */
public class WriteUrl2Txt {

    public static final int UNSUPPORT_TYPE = 1;
    public static final int FILE_NOT_EXIT = 2;
    public static String resDir;

    static {      
        resDir = "html_src/";
        
//        File file = new File(resDir);
//        if (!file.exists()) {
//            file.mkdirs();
//        }
    }
    
    //��url������ȡ���д���ı��ļ�

    public void write(String fileName, String urlResult) {

        File file = new File(resDir + fileName + ".txt");
        //file.deleteOnExit();
        Writer pw = null;
        try {
            pw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            pw.write(urlResult);
            pw.flush();

        } catch (IOException ex) {
            Logger.getLogger(WriteUrl2Txt.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (pw != null) {
                try {
					pw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        }
    }
    
    public void write(String fileName, BufferedReader in) {
    	File file = new File(resDir + fileName + ".txt");
        //file.deleteOnExit();
    	BufferedWriter pw = null;
        try {
            pw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            String line = "";
            while((line = in.readLine())!=null){
            	pw.write(line);
            	pw.newLine();
            }
            pw.flush();

        } catch (IOException ex) {
            Logger.getLogger(WriteUrl2Txt.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (pw != null) {
                try {
					pw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
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
            //ֻ֧��txt�ļ��Ķ�д
            return UNSUPPORT_TYPE;
        }
        File file = new File(path);

        if (!file.exists()) {
            //�����ڴ��ļ�
            return FILE_NOT_EXIT;
        }
        //���ж���url
        return 0;

    }

    public static void main(String[] args) {
        //�����Ƿ�����Ψһ��.
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

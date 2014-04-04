/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scutdm.summary.crawler.core;

import java.io.PipedReader;
import java.io.PipedWriter;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;

import com.scutdm.summary.crawler.io.SeedFileMgt;
import com.scutdm.summary.crawler.io.URLManageFactory;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

/**
 *
 * @author Administrator
 */
public class Cral2FileController {

    public static final String CRAWL_ENDED = "crawl ended";
    private CrawlController controller;
    private int numOfThreads;
    private PipedReader reader;
    // private List<WritableComponent> handlors = new ArrayList<WritableComponent>();
    private WritableComponent handler;
    private SeedFileMgt fileMgt;

    public Cral2FileController() throws Exception {
        
        fileMgt = URLManageFactory.getURLMangeObj();
        if (CrawlProperties.contains("crawl.num_of_threads")) {
            numOfThreads = Integer.parseInt(CrawlProperties.getProperty("crawl.num_of_threads"));
        } else {
            numOfThreads = 5;
        }
        CrawlConfig config = new CrawlConfig();
        if (CrawlProperties.contains("crawl.storage.path")) {
            config.setCrawlStorageFolder(CrawlProperties.getProperty("crawl.storage.path"));
        } else {
            config.setCrawlStorageFolder("crawl_stroage");
        }

        /*
         * Be polite: Make sure that we don't send more than 1 request per
         * second (1000 milliseconds between requests).
         */
        config.setPolitenessDelay(1000);

        /*
         * You can set the maximum crawl depth here. The default value is -1 for
         * unlimited depth
         */
        config.setMaxDepthOfCrawling(1);

        /*
         * You can set the maximum number of pages to crawl. The default value
         * is -1 for unlimited number of pages
         */
        config.setMaxPagesToFetch(1000);

        /*
         * Do you need to set a proxy? If so, you can use:
         * config.setProxyHost("proxyserver.example.com");
         * config.setProxyPort(8080);
         *
         * If your proxy also needs authentication:
         * config.setProxyUsername(username); config.getProxyPassword(password);
         */
        config.setProxyHost("127.0.0.1");
        config.setProxyPort(8087);

        /*
         * This config parameter can be used to set your crawl to be resumable
         * (meaning that you can resume the crawl from a previously
         * interrupted/crashed crawl). Note: if you enable resuming feature and
         * want to start a fresh crawl, you need to delete the contents of
         * rootFolder manually.
         */
        config.setResumableCrawling(false);

        /*
         * Instantiate the controller for this crawl.
         */
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        controller = new CrawlController(config, pageFetcher, robotstxtServer);

        //调用controller的addSeed增加URL,并利用start方法开始爬取
        //controller.addSeed("http://news.sina.com.cn/c/2012-07-27/063524854410.shtml");
        int size = fileMgt.getSize();
        for (int i = 0; i < size; i++) {
            controller.addSeed(fileMgt.getUrl(i));
        }
        fileMgt = URLManageFactory.getURLMangeObj();
        
        //startCrawl();
    }

    public Cral2FileController(WritableComponent textComponent) throws Exception {
        
        this();
        this.fileMgt = fileMgt;
        handler = textComponent;
        //配置日子的输出到textComponent;
        Logger rootLogger = Logger.getRootLogger();

        Appender appender = rootLogger.getAppender("R");
        reader = new PipedReader();
        PipedWriter writer = new PipedWriter(reader);
        ((WriterAppender) appender).setWriter(writer);
        handler.handleOutput(reader);


    }

    public void startCrawl() {

        controller.start(Crawl2File.class, numOfThreads);
        endCrawl();

    }
    /*
     * 启动无阻塞启动
     */

    public void startCrawlNoBlocking() {
        controller.startNonBlocking(Crawl2File.class, numOfThreads);
        new Thread(new Runnable(){

            @Override
            public void run() {
                controller.waitUntilFinish();
                endCrawl();
            }
            
        }).start();
    }
    /**
     * 结束爬取
     */
    public void endCrawl(){
        Logger.getRootLogger().info(CRAWL_ENDED);
    }
    
//    public class WriteThread extends Thread{
//        Reader reader;
//        WritableComponent comp;
//        public WriteThread(Reader reader,WritableComponent comp){
//            this.reader = reader;
//            this.comp = comp;
//            comp.setWriteThread(this);
//        }
//        @Override
//        public void run(){
//            //检测reader并输出
//            Scanner scanner = new Scanner(reader);
//            
//            while(scanner.hasNext()){
//                comp.write(scanner.nextLine());
//            }
//        }
//    }

    public static void main(String[] args) {
        try {
            new Cral2FileController().startCrawl();
        } catch (Exception ex) {
            ex.printStackTrace();
            // Logger.getLogger(Cral2FileController.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("end crawling");
    }
}

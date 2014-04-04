/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scutdm.summary.crawler.core;


import java.io.Reader;

/**
 *
 * @author Administrator
 */
public interface WritableComponent {
   
    
    /*
     * 接受appender的输出
     */
    public void handleOutput(Reader reader);
    
}

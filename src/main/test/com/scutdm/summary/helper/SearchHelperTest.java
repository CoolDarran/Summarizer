package com.scutdm.summary.helper;

import org.junit.Test;

import junit.framework.TestCase;

public class SearchHelperTest extends TestCase {
	@Test
	public void searchGoogleTest() throws Exception{
		System.out.println("test begin");
		SearchHelper.searchGoogle("acfun bilibili");
		assertEquals(1, 1);
	}
}

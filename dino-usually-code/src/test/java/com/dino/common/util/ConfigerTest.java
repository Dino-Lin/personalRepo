package com.dino.common.util;

import org.testng.annotations.Test;

import junit.framework.TestCase;

public class ConfigerTest extends TestCase {
	@Test
	public void testGetProperty(){
		System.out.println(Configer.getProperty("systemName"));
	}
}

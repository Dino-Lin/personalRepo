package com.dino.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class Configer {
	/*
	 * 配置文件路径
	 */
	private static final String cfgFile = "/config.properties";
	/**
	 * 读出的属性
	 */
	private static Properties properties;

	private Configer() {

	}

	static {
		properties = new Properties();
		InputStream is = Configer.class.getResourceAsStream(cfgFile);
		try {
			properties.load(is);
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("读取propertise属性文件失败，请重试！");
		}finally{
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 读取文件属性
	 * @param propertyName
	 * @return
	 */
	public static String getProperty(String propertyName) {
		if (properties == null) {
			throw new RuntimeException("系统错误：读取config属性文件失败！");
		} else {
			return properties.getProperty(propertyName);
		}
	}
}

package com.wdx.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

@SuppressWarnings("all")
public class PropertiesUtils {
	
	private final static Logger logger = Logger.getLogger(PropertiesUtils.class);
	
	public static String getValue(String path, String key) {
		String result = "";
		try {
			if (StringUtils.isNotBlank(path)) {
				Properties properties = new Properties();
				InputStream inputStream = PropertiesUtils.class
						.getResourceAsStream(path);
				properties.load(inputStream);
				result = properties.getProperty(key);
				inputStream.close();
			}
		} catch (IOException e) {
			logger.error(e);
		}
		return result;
	}

	public static Map<String, String> getPropertiesValue(String path) {
		Map map = null;
		try {
			if (StringUtils.isNotBlank(path)) {
				map = new HashMap();
				Properties properties = new Properties();
				InputStream inputStream = PropertiesUtils.class
						.getResourceAsStream(path);
				properties.load(inputStream);
				for (Iterator iterator = properties.keySet().iterator(); iterator
						.hasNext();) {
					String key = convertToStr(iterator.next());
					String value = convertUnicodeToUtf8(properties
							.get(key));
					map.put(key, value);
				}
				inputStream.close();
			}
		} catch (IOException e) {
			logger.error(e);
		}
		return map;
	}
	
	private static String convertToStr(Object obj) {
		String str = "";
		if (StringUtils.isNotEmpty(obj.toString())) {
			str = obj.toString().trim();
		}
		return str;
	}

	private static String convertUnicodeToUtf8(Object obj) {
		try {
			return new String(convertToStr(obj).getBytes("ISO8859_1"), "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "";
	}
	
}
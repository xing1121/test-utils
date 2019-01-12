package com.wdx.utils.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * 描述：属性文件工具类
 * @author 80002888
 * @date   2019年1月12日
 */
public class PropertiesUtils {
	
	private final static Logger logger = Logger.getLogger(PropertiesUtils.class);
	
	/**
	 * 从类路径下的属性文件中获取int类型的值
	 *	@ReturnType	int 
	 *	@Date	2019年1月12日	下午2:47:57
	 *  @Param  @param path
	 *  @Param  @param key
	 *  @Param  @param defaultValue
	 *  @Param  @return
	 */
	public static int getIntValue(String path, String key, int defaultValue) {
		String str = getValue(path, key);
		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException e) {
			logger.error(key, e);
		}
		return defaultValue;
	}
	
	/**
	 * 从类路径下的属性文件中获取boolean类型的值
	 *	@ReturnType	int 
	 *	@Date	2019年1月12日	下午2:47:57
	 *  @Param  @param path
	 *  @Param  @param key
	 *  @Param  @param defaultValue
	 *  @Param  @return
	 */
	public static boolean getBooleanValue(String path, String key, boolean defaultValue) {
		String str = getValue(path, key);
		try {
			return Boolean.parseBoolean(str);
		} catch (NumberFormatException e) {
			logger.error(key, e);
		}
		return defaultValue;
	}
	
	/**
	 * 从类路径下的属性文件中获取String类型的值
	 *	@ReturnType	String 
	 *	@Date	2019年1月12日	下午2:49:09
	 *  @Param  @param path
	 *  @Param  @param key
	 *  @Param  @return
	 */
	public static String getValue(String path, String key) {
		String result = null;
		try {
			if (StringUtils.isNotBlank(path)) {
				Properties properties = new Properties();
				InputStream inputStream = PropertiesUtils.class.getResourceAsStream(path);
				properties.load(inputStream);
				result = properties.getProperty(key);
				inputStream.close();
			}
		} catch (IOException e) {
			logger.error(e);
		}
		return result;
	}

	/**
	 * 获取指定属性文件中所以key=value
	 *	@ReturnType	Map<String,String> 
	 *	@Date	2019年1月12日	下午2:49:33
	 *  @Param  @param path
	 *  @Param  @return
	 */
	public static Map<String, String> getPropertiesValue(String path) {
		Map<String, String> map = null;
		try {
			if (StringUtils.isNotBlank(path)) {
				map = new HashMap<>();
				Properties properties = new Properties();
				InputStream inputStream = PropertiesUtils.class.getResourceAsStream(path);
				properties.load(inputStream);
				Iterator<Object> iterator = properties.keySet().iterator();
				while (iterator.hasNext()) {
					String key = convertToStr(iterator.next());
					String value = convertUnicodeToUtf8(properties.get(key));
					map.put(key, value);
				}
				inputStream.close();
			}
		} catch (IOException e) {
			logger.error(e);
		}
		return map;
	}
	
	/**
	 * 对象转字符串
	 *	@ReturnType	String 
	 *	@Date	2019年1月12日	下午2:50:31
	 *  @Param  @param obj
	 *  @Param  @return
	 */
	private static String convertToStr(Object obj) {
		String str = "";
		if (StringUtils.isNotEmpty(obj.toString())) {
			str = obj.toString().trim();
		}
		return str;
	}

	/**
	 * 对象转UTF-8字符串
	 *	@ReturnType	String 
	 *	@Date	2019年1月12日	下午2:50:31
	 *  @Param  @param obj
	 *  @Param  @return
	 */
	private static String convertUnicodeToUtf8(Object obj) {
		try {
			return new String(convertToStr(obj).getBytes("ISO8859_1"), "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "";
	}
	
}
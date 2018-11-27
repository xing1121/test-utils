package com.wdx.utils.sql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * 描述：从文件中获取sql字符串
 * @author 80002888
 * @date   2018年11月26日
 */
public class MySqlUtils {

	private final static Logger logger = Logger.getLogger(MySqlUtils.class);
	
	/**
	 * 从类路径的文件中获取sql字符串
	 *	@ReturnType	String 
	 *	@Date	2018年11月26日	下午4:50:00
	 *  @Param  @param path			如：/sql/pickUpWarningMonitor-useJoin.sql
	 *  @Param  @return
	 */
	public static MySql getSqlFromClassPath(String path){
		BufferedReader br = null;
		try {
			// 读取文件流
			URL resource = MySqlUtils.class.getResource(path);
			br = new BufferedReader(new FileReader(new File(resource.getPath())));
			// 获取内容
			StringBuffer sb = new StringBuffer();
			String content = null;
			while (StringUtils.isNotBlank(content = br.readLine())) {
				if (content.trim().startsWith("--") || content.startsWith("#")) {
					continue;
				}
				sb.append(content);
				sb.append("\n");
			}
			content = sb.toString().trim();
			if (StringUtils.isBlank(content)) {
				return null;
			}
			if (content.endsWith(";")) {
				content = content.substring(0, content.length() - 1);
			}
			content.replace(";", "");
			return new MySql(content);
		} catch (Exception e) {
			logger.error("get error->" + path, e);
		} finally {
			// 关闭流
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					logger.error("get error->", e);
				}
			}
		}
		return null;
	}
	
	/**
	 * 从类路径的文件中获取字符串
	 *	@ReturnType	String 
	 *	@Date	2018年11月27日	上午11:47:35
	 *  @Param  @param path
	 *  @Param  @return
	 */
	public static String getStringFromClassPath(String path){
		BufferedReader br = null;
		try {
			// 读取文件流
			URL resource = MySqlUtils.class.getResource(path);
			br = new BufferedReader(new FileReader(new File(resource.getPath())));
			// 获取内容
			StringBuffer sb = new StringBuffer();
			String content = null;
			while ((content = br.readLine())!=null) {
				sb.append(content);
				sb.append("\n");
			}
			content = sb.toString();
			if (StringUtils.isBlank(content)) {
				return null;
			}
			return content;
		} catch (Exception e) {
			logger.error("get error->" + path, e);
		} finally {
			// 关闭流
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					logger.error("get error->", e);
				}
			}
		}
		return null;
	}
	
}

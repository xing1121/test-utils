package com.wdx.utils;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wdx.utils.http.HttpResult;

/**
 * 描述：Http请求工具类
 * @author 80002888
 * @date   2018年4月23日
 */
public class HttpClientUtils {
	
	private final static Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

	public static void main(String[] args) {
		try {
			String url = "http://www.baidu.com";
			Map<String, String> params = new HashMap<>();
			params.put("name", "java");
			params.put("hello", "world");
			HttpResult httpResult = HttpClientUtils.doPost(url, params);
			logger.info("响应状态码：" + httpResult.getStatus());
			logger.info("响应内容：" + httpResult.getContent());
		} catch (Exception e) {
			logger.error("get error->",e);
		}
	}
	
	/**
	 * 发送get请求
	 * @throws Exception 
	 *	@ReturnType	HttpResult 
	 *	@Date	2018年4月23日	下午3:51:26
	 *  @Param  @param url			请求地址
	 *  @Param  @return
	 */
	public static HttpResult doGet(String url) throws Exception{
		if (StringUtils.isBlank(url)) {
			return null;
		}
        // 创建Httpclient对象
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建http GET请求
        HttpGet httpGet = new HttpGet(url);
        // 响应对象
        CloseableHttpResponse response = null;
        // 自定义响应对象
        HttpResult httpResult = null;
        try {
            // 执行请求，得到响应
        	logger.info("Request URL(Get)："+url);
            response = httpclient.execute(httpGet);
            if (response == null) {
				return null;
			}
            httpResult = new HttpResult();
            // 设置响应状态码
            Integer statusCode = response.getStatusLine().getStatusCode();
            httpResult.setStatus(statusCode);
            // 设置响应内容
            HttpEntity httpEntity = response.getEntity();
            if (httpEntity != null) {
            	String content = EntityUtils.toString(response.getEntity(), "UTF-8");
            	httpResult.setContent(content);
			}
        } catch(Exception e) {
        	throw e;
        } finally {
            if (response != null) {
                response.close();
            }
            httpclient.close();
        }
		return httpResult;
	}
	
	/**
	 * 发送get请求，带参数
	 * @throws Exception 
	 *	@ReturnType	HttpResult 
	 *	@Date	2018年4月23日	下午3:51:32
	 *  @Param  @param url			请求地址
	 *  @Param  @param params		参数组成的map(k=v)
	 *  @Param  @return
	 */
	public static HttpResult doGet(String url, Map<String, String> params) throws Exception{
		if (StringUtils.isBlank(url)) {
			return null;
		}
        // URI构造器
        URIBuilder uriBuilder = new URIBuilder(url);
        // 封装参数
        if (params != null && params.size() != 0) {
        	Set<Entry<String, String>> entrySet = params.entrySet();
        	for (Entry<String, String> entry : entrySet) {
        		String key = entry.getKey();
        		String value = entry.getValue();
        		if (entry == null || StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
					continue;
				}
        		uriBuilder.setParameter(key, value);
			}
		}
        // 构造URI
        URI uri = uriBuilder.build();
        // 创建Httpclient对象
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建http GET请求
        HttpGet httpGet = new HttpGet(uri);
        // 响应对象
        CloseableHttpResponse response = null;
        // 自定义响应对象
        HttpResult httpResult = null;
        try {
            // 执行请求，得到响应
        	logger.info("Request URI(Get)："+uri.toString());
            response = httpclient.execute(httpGet);
            if (response == null) {
				return null;
			}
            httpResult = new HttpResult();
            // 设置响应状态码
            Integer statusCode = response.getStatusLine().getStatusCode();
            httpResult.setStatus(statusCode);
            // 设置响应内容
            HttpEntity httpEntity = response.getEntity();
            if (httpEntity != null) {
            	String content = EntityUtils.toString(response.getEntity(), "UTF-8");
            	httpResult.setContent(content);
			}
        } catch(Exception e) {
        	throw e;
        } finally {
            if (response != null) {
                response.close();
            }
            httpclient.close();
        }
		return httpResult;
	}
	
	/**
	 * 发送post请求
	 * @throws Exception 
	 *	@ReturnType	HttpResult 
	 *	@Date	2018年4月23日	下午3:51:53
	 *  @Param  @param url			请求地址
	 *  @Param  @return
	 */
	public static HttpResult doPost(String url) throws Exception{
		if (StringUtils.isBlank(url)) {
			return null;
		}
        // 创建Httpclient对象
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建http GET请求
        HttpPost httpPost = new HttpPost(url);
        // 响应对象
        CloseableHttpResponse response = null;
        // 自定义响应对象
        HttpResult httpResult = null;
        try {
            // 执行请求，得到响应
        	logger.info("Request URL(POST)："+url);
            response = httpclient.execute(httpPost);
            if (response == null) {
				return null;
			}
            httpResult = new HttpResult();
            // 设置响应状态码
            Integer statusCode = response.getStatusLine().getStatusCode();
            httpResult.setStatus(statusCode);
            // 设置响应内容
            HttpEntity httpEntity = response.getEntity();
            if (httpEntity != null) {
            	String content = EntityUtils.toString(response.getEntity(), "UTF-8");
            	httpResult.setContent(content);
			}
        } catch(Exception e) {
        	throw e;
        } finally {
            if (response != null) {
                response.close();
            }
            httpclient.close();
        }
		return httpResult;
	}
	
	/**
	 * 发送post请求，带参数
	 * @throws Exception 
	 *	@ReturnType	HttpResult 
	 *	@Date	2018年4月23日	下午3:52:01
	 *  @Param  @param url			请求地址
	 *  @Param  @param params		参数组成的map(k=v)
	 *  @Param  @return
	 */
	public static HttpResult doPost(String url, Map<String, String> params) throws Exception{
		if (StringUtils.isBlank(url)) {
			return null;
		}
        // 创建Httpclient对象
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建自定义响应对象
        HttpResult httpResult = null;
        // 创建POST请求
        HttpPost httpPost = new HttpPost(url);
        // 响应对象
        CloseableHttpResponse response = null;
        // 构造请求体，封装进httpPost
        if (params != null && params.size() != 0) {
            List<NameValuePair> parameters = new ArrayList<NameValuePair>(10);
        	Set<Entry<String, String>> entrySet = params.entrySet();
        	for (Entry<String, String> entry : entrySet) {
        		String key = entry.getKey();
        		String value = entry.getValue();
        		if (entry == null || StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
					continue;
				}
        		parameters.add(new BasicNameValuePair(key, value));
			}
        	if (parameters != null && parameters.size() != 0) {
        		// 构造一个form表单式的实体
        		UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters,"UTF-8");
        		// 将请求实体设置到httpPost对象中
        		httpPost.setEntity(formEntity);
			}
		}
        try {
        	 // 执行请求，得到响应
        	logger.info("Request URL(POST)："+url);
        	logger.info("Request Params(POST)："+params);
            response = httpclient.execute(httpPost);
            if (response == null) {
				return null;
			}
            httpResult = new HttpResult();
            // 设置响应状态码
            Integer statusCode = response.getStatusLine().getStatusCode();
            httpResult.setStatus(statusCode);
            // 设置响应内容
            HttpEntity httpEntity = response.getEntity();
            if (httpEntity != null) {
            	String content = EntityUtils.toString(response.getEntity(), "UTF-8");
            	httpResult.setContent(content);
			}
        } finally {
            if (response != null) {
                response.close();
            }
            httpclient.close();
        }
        return httpResult;
	}
	
}

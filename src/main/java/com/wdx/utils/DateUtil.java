package com.wdx.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateUtil extends DateUtils{
	
	private static final Logger logger = LoggerFactory.getLogger(DateUtil.class);
	
	/**
	 * 格式化日期为指定格式的String
	 *	@ReturnType	String 
	 *	@Date	2018年4月23日	上午10:56:30
	 *  @Param  @param date
	 *  @Param  @param formatStr
	 *  @Param  @return
	 */
	public static String dateFomat(Date date,String formatStr){
		String dateStr = null;
		if(null != date && StringUtils.isNotEmpty(formatStr)){
			DateFormat df = new SimpleDateFormat(formatStr);
	        dateStr = df.format(date);
	        return dateStr;
		}
		return dateStr;
	}
	
	/**
	 * 日期拼接，date+time，返回Date
	 *	@ReturnType	Date 
	 *	@Date	2018年4月9日	上午9:59:44
	 *  @Param  @param date
	 *  @Param  @param time
	 *  @Param  @return
	 */
	public static Date concatDate(Date date, Date time){
		try {
			DateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");
			DateFormat dfTime = new SimpleDateFormat("HH:mm:ss");
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String dateStr = dfDate.format(date);
			String timeStr = dfTime.format(time);
			String res = dateStr + " " + timeStr;
			return df.parse(res);
		} catch (Exception e) {
			logger.error("--------DateUtil...concatDate 发生异常："+e.getMessage(),e);
		}
		return null;
	}
	
	/**
	 * 日期拼接，date+time，返回String
	 *	@ReturnType	String 
	 *	@Date	2018年4月10日	下午1:59:26
	 *  @Param  @param date
	 *  @Param  @param time
	 *  @Param  @return
	 */
	public static String concatDateToStr(Date date, Date time){
		try {
			DateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");
			DateFormat dfTime = new SimpleDateFormat("HH:mm:ss");
			String dateStr = dfDate.format(date);
			String timeStr = dfTime.format(time);
			String res = dateStr + " " + timeStr;
			return res;
		} catch (Exception e) {
			logger.error("--------DateUtil...concatDateToStr 发生异常："+e.getMessage(),e);
		}
		return null;
	}
	
	/**
	 * 获取指定时间+hours的时间的字符串"yyyy-MM-dd HH:mm:ss"
	 *	@ReturnType	String 
	 *	@Date	2018年4月1日	上午11:02:21
	 *  @Param  @param date
	 *  @Param  @param hours
	 *  @Param  @return
	 *  @Param  @throws ParseException
	 */
	public static String dateAddHours(String date, Integer hours) throws ParseException {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar Cal = Calendar.getInstance();
		Cal.setTime(DateUtils.parseDate(date, "yyyy-MM-dd HH:mm:ss"));
		Cal.add(java.util.Calendar.HOUR_OF_DAY, hours);
		return df.format(Cal.getTime());
	}
	
	
	/**
	 * 返回日期，不带时间
	 *	@ReturnType	Date 
	 *	@Date	2018年4月2日	下午7:32:03
	 *  @Param  @param dateStr
	 *  @Param  @return
	 */
	public static Date getDate(String dateStr,String pattern){
		// 转为时间
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		TemporalAccessor temporalAccessor = dtf.parse(dateStr);
		// 转为String
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String s = dateFormat.format(temporalAccessor);
		// 转为时间
		Date date = Date.from(LocalDateTime.from(dateFormat.parse(s)).atZone((ZoneId.systemDefault())).toInstant());
		return date;
	}
	
	/**
	 * 返回时间，不带日期
	 *	@ReturnType	Date 
	 *	@Date	2018年4月2日	下午7:32:03
	 *  @Param  @param dateStr
	 *  @Param  @return
	 */
	public static Date getTime(String dateStr,String pattern){
		// 转为时间
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		TemporalAccessor temporalAccessor = dtf.parse(dateStr);
		// 转为String
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
		String s = dateFormat.format(temporalAccessor);
		// 转为时间
		Date date = Date.from(LocalDateTime.from(dateFormat.parse(s)).atZone((ZoneId.systemDefault())).toInstant());
		return date;
	}

	/**
	 * string转date
	 *	@ReturnType	Date 
	 *	@Date	2018年4月2日	下午7:55:14
	 *  @Param  @param dateStr
	 *  @Param  @param pattern
	 *  @Param  @return
	 */
	public static Date formatDate(String dateStr, String pattern) {
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(pattern);
		return Date.from(LocalDateTime.from(dateFormat.parse(dateStr)).atZone((ZoneId.systemDefault())).toInstant());
	}
	
	
	/**
	 * 格林威治时间转当地时间
	 */
	public static Date greenwichTimeToLocalTime(String countryAbb, Date greenwichTime) {
		if (StringUtils.isEmpty(countryAbb) || greenwichTime == null) {
			logger.warn("----------DateUtil...greenwichTimeToLocalTime... 参数："+countryAbb+","+greenwichTime+"为空，无法进行时间转换！");
			return null;
		}
		Calendar localTime = Calendar.getInstance();
		localTime.setTime(greenwichTime);
		switch (countryAbb) {
		case "CN":
			localTime.add(Calendar.HOUR_OF_DAY, 8);
			break;
		case "US":
			localTime.add(Calendar.HOUR_OF_DAY, -5);
			break;
		case "SG":
			localTime.add(Calendar.HOUR_OF_DAY, 8);
			break;
		case "MY":
			localTime.add(Calendar.HOUR_OF_DAY, 8);
			break;
		case "JP":
			localTime.add(Calendar.HOUR_OF_DAY, 9);
			break;
		case "KR":
			localTime.add(Calendar.HOUR_OF_DAY, 9);
			break;
		case "TH":
			localTime.add(Calendar.HOUR_OF_DAY, 7);
			break;
		default:
			logger.warn("----------DateUtil...greenwichTimeToLocalTime...countryConstant没有配置指定国家："+countryAbb+"的信息，无法进行时间转换！");
			return null;
		}
		return localTime.getTime();
	}

	/**
	 * 当地时间转北京时间
	 */
	public static Date localTimeToChinaTime(String countryAbb, Date localTime){
		if (StringUtils.isEmpty(countryAbb) || localTime == null) {
			logger.warn("----------DateUtil...localTimeToChinaTime... 参数："+countryAbb+","+localTime+"为空，无法进行时间转换！");
			return null;
		}
		Date greenwichTime = localTimeToGreenwichTime(countryAbb, localTime);
		Date chinaTime = greenwichTimeToLocalTime("CN", greenwichTime);
		return chinaTime;
	}
	
	/**
	 * 北京时间转当地时间
	 */
	public static Date chinaTimeToLocalTime(String countryAbb, Date chinaTime){
		if (StringUtils.isEmpty(countryAbb) || chinaTime == null) {
			logger.warn("--------DateUtil...localTimeToChinaTime... 参数："+countryAbb+","+chinaTime+"为空，无法进行时间转换！");
			return null;
		}
		Date greenwichTime = localTimeToGreenwichTime("CN", chinaTime);
		Date localTime = greenwichTimeToLocalTime(countryAbb, greenwichTime);
		return localTime;
	}
	
	/**
	 * 当地时间转格林威治时间
	 */
	public static Date localTimeToGreenwichTime(String countryAbb, Date localTime){
		if (StringUtils.isEmpty(countryAbb) || localTime == null) {
			logger.warn("--------DateUtil...localTimeToGreenwichTime... 参数："+countryAbb+","+localTime+"为空，无法进行时间转换！");
			return null;
		}
		Calendar greenwichTime = Calendar.getInstance();
		greenwichTime.setTime(localTime);
		switch (countryAbb) {
		case "CN":
			greenwichTime.add(Calendar.HOUR_OF_DAY, -8);
			break;
		case "US":
			greenwichTime.add(Calendar.HOUR_OF_DAY, 5);
			break;
		case "SG":
			greenwichTime.add(Calendar.HOUR_OF_DAY, -8);
			break;
		case "MY":
			greenwichTime.add(Calendar.HOUR_OF_DAY, -8);
			break;
		case "JP":
			greenwichTime.add(Calendar.HOUR_OF_DAY, -9);
			break;
		case "KR":
			greenwichTime.add(Calendar.HOUR_OF_DAY, -9);
			break;
		case "TH":
			greenwichTime.add(Calendar.HOUR_OF_DAY, -7);
			break;
		default:
			logger.warn("--------DateUtil...localTimeToGreenwichTime...没有配置指定国家："+countryAbb+"的时间信息，无法进行时间转换！");
			return null;
		}
		return greenwichTime.getTime();
	}
	
}

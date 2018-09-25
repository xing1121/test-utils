package com.wdx.utils.date;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

/**
 * 描述：时间工具类
 * @author 80002888
 * @date   2018年9月25日
 */
public class DateUtil extends DateUtils{
	
	/**
	 * 日期时间
	 */
	private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
	
	/**
	 * 纯日期
	 */
	private static final String DATE_PATTERN = "yyyy-MM-dd"; 
	
	/**
	 * 纯时间
	 */
	private static final String TIME_PATTERN = "HH:mm:ss"; 
	
	public static void main(String[] args) throws Exception {
		// 现在时间
		Date now = new Date();
		System.out.println("现在时间：" + now);
		
		System.out.println("------------------------------------------");
		
		// 测试日期时间转字符串
		String dateTimeStr = date2Str(now, DATE_TIME_PATTERN);
		System.out.println(dateTimeStr);
		
		// 测试字符串转日期时间
		Date dateTime = str2Date(dateTimeStr, DATE_TIME_PATTERN);
		System.out.println(dateTime);
		
		System.out.println("------------------------------------------");
		
		// 测试日期转字符串
		String dateStr = date2Str(now, DATE_PATTERN);
		System.out.println(dateStr);
		
		// 测试字符串转日期
		Date date = str2Date(dateStr, DATE_PATTERN);
		System.out.println(date);
		
		System.out.println("------------------------------------------");
		
		// 测试时间转字符串
		String timeStr = date2Str(now, TIME_PATTERN);
		System.out.println(timeStr);
		
		// 测试字符串转时间
		Date time = str2Date(timeStr, TIME_PATTERN);
		System.out.println(time);
		
		System.out.println("------------------------------------------");
		
		// 测试日期拼接
		Date concatDate = concatDate(date, time);
		System.out.println(concatDate);
		
		// 时间加上毫秒
		Date addDate = dateAddMillSeconds(now, (1000 * 60 * 60));
		System.out.println(addDate);
		
		System.out.println("------------------------------------------");
		
		// 测试返回日期
		System.out.println(DateUtil.getDate(now));
		
		// 测试返回时间
		System.out.println(DateUtil.getTime(now));
	}
	
	/**
	 * 时间转字符串
	 *	@ReturnType	String 
	 *	@Date	2018年9月25日	下午5:56:20
	 *  @Param  @param date
	 *  @Param  @param pattern
	 *  @Param  @return
	 */
	public static String date2Str(Date date, String pattern){
		if (StringUtils.isBlank(pattern) || date == null) {
			return null;
		}
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
		return dtf.format(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
	}
	
	/**
	 * 字符串转日期/时间
	 * 		如果dateStr中的英文字母只包含M、d、y、Y，则认定为纯日期。
	 * 		如果dateStr中的英文字母不包含M、d、y、Y，则认定为纯时间。
	 *	@ReturnType	Date 
	 *	@Date	2018年9月25日	下午6:22:25
	 *  @Param  @param dateStr
	 *  @Param  @param pattern
	 *  @Param  @return
	 */
	public static Date str2Date(String dateStr, String pattern){
		if (StringUtils.isBlank(pattern) || StringUtils.isBlank(dateStr)) {
			return null;
		}
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
		boolean haveDateFlag = false;
		boolean haveTimeFlag = false;
		Pattern letterPattern = Pattern.compile("^[A-Za-z]+");
		Pattern datePattern = Pattern.compile("^[yYMd]+");
		for (int i = 0; i < pattern.length(); i++) {
			String c = String.valueOf(pattern.charAt(i));
			// 是英文字符
			if (letterPattern.matcher(c).matches()) {
				if (datePattern.matcher(c).matches()) {
					// 包含日期
					haveDateFlag = true;
				} else {
					// 包含时间
					haveTimeFlag = true;
				}
			}
		}
		// 纯日期
		if (haveDateFlag && !haveTimeFlag) {
			return Date.from(LocalDate.parse(dateStr, dtf).atTime(LocalTime.of(0, 0, 0)).atZone((ZoneId.systemDefault())).toInstant());
		}
		// 纯时间（默认日期为1970-01-01）
		if (!haveDateFlag && haveTimeFlag) {
			return Date.from(LocalTime.parse(dateStr, dtf).atDate(LocalDate.ofEpochDay(0)).atZone((ZoneId.systemDefault())).toInstant());
		}
		// 日期+时间
		return Date.from(LocalDateTime.from(dtf.parse(dateStr)).atZone((ZoneId.systemDefault())).toInstant());
	}
	
	/**
	 * 日期拼接，date + time，返回Date
	 *	@ReturnType	Date 
	 *	@Date	2018年9月25日	下午5:57:26
	 *  @Param  @param date
	 *  @Param  @param time
	 *  @Param  @return
	 *  @Param  @throws Exception
	 */
	public static Date concatDate(Date date, Date time) throws Exception{
		String dateStr = date2Str(date, DATE_PATTERN);
		String timeStr = date2Str(time, TIME_PATTERN);
		String dateTimeStr = dateStr + " " + timeStr;
		return str2Date(dateTimeStr, DATE_TIME_PATTERN);
	}
	
	/**
	 * 获取指定时间 + 毫秒的时间
	 *	@ReturnType	Date 
	 *	@Date	2018年9月25日	下午7:01:57
	 *  @Param  @param date
	 *  @Param  @param millSeconds
	 *  @Param  @return
	 *  @Param  @throws ParseException
	 */
	public static Date dateAddMillSeconds(Date date, Integer millSeconds) throws ParseException {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MILLISECOND, millSeconds);
		return cal.getTime();
	}
	
	/**
	 * 返回日期（时间默认为00:00:00）
	 *	@ReturnType	Date 
	 *	@Date	2018年9月25日	下午7:11:05
	 *  @Param  @param date
	 *  @Param  @return
	 */
	public static Date getDate(Date date){
		// 转为String
		String dateStr = date2Str(date, DATE_PATTERN);
		// 转为日期
		return str2Date(dateStr, DATE_PATTERN);
	}
	
	/**
	 * 返回时间（日期默认为1970-01-01）
	 *	@ReturnType	Date 
	 *	@Date	2018年9月25日	下午7:11:00
	 *  @Param  @param date
	 *  @Param  @return
	 */
	public static Date getTime(Date date){
		// 转为String
		String dateTimeStr = date2Str(date, TIME_PATTERN);
		// 转为时间
		return str2Date(dateTimeStr, TIME_PATTERN);
	}

}

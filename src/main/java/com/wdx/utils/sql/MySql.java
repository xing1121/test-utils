package com.wdx.utils.sql;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.wdx.utils.date.DateUtil;

/**
 * 描述：自定义StmsSql，用来获取sql的字符串
 * @author 80002888
 * @date   2018年11月26日
 */
public class MySql {
	
	private String sqlStr;

	private Map<Integer, String> parameters;
	
	public MySql(String sqlStr) {
		parameters = new HashMap<Integer, String>();
		this.sqlStr = sqlStr;
	}

	/**
	 * 设置参数 -- 支持字符串、数字、Date、布尔（true-1，false-0），其他默认toString()
	 *	@ReturnType	void 
	 *	@Date	2018年11月26日	下午5:21:04
	 *  @Param  @param position			占位符的索引，从0开始
	 *  @Param  @param val				值
	 */
	public void setParameter(int position, Object val){
		String value = getRealValueFromObj(val);
		parameters.put(position, value);
	}
	
	/**
	 * 获取填充值后的sql语句
	 *	@ReturnType	String 
	 *	@Date	2018年11月26日	下午5:21:36
	 *  @Param  @return
	 */
	public String getSqlStr() {
		char[] charArray = sqlStr.toCharArray();
		List<String> strList = charArrayToStringList(charArray);
		List<String> listOfQuestionMark = strList.stream().filter("?"::equals).collect(Collectors.toList());
		if (listOfQuestionMark != null && (listOfQuestionMark.size() != parameters.size())) {
			throw new RuntimeException("占位符数量【" + listOfQuestionMark.size()  + "】与参数数量【" + parameters.size() + "】不一致！");
		}
		String res = "";
		int i = 0;
		for (String str : strList) {
			if ("?".equals(str)) {
				str = parameters.get(i++);
			}
			res += str;
		}
		return res;
	}
	
	/**
	 * 数组转集合
	 *	@ReturnType	List<String> 
	 *	@Date	2018年11月26日	下午6:05:29
	 *  @Param  @param charArray
	 *  @Param  @return
	 */
	private List<String> charArrayToStringList(char[] charArray){
		List<String> list = new ArrayList<>();
		for (char c : charArray) {
			String s = String.valueOf(c);
			list.add(s);
		}
		return list;
	}
	
	/**
	 * Object的值转String
	 *	@ReturnType	String 
	 *	@Date	2018年11月26日	下午5:24:37
	 *  @Param  @param obj
	 *  @Param  @return
	 */
	private String getRealValueFromObj(Object obj){
		// null
		if (obj == null) {
			return "null";
		}
		// 字符串
		if (obj instanceof String) {
			return "'" + obj.toString() + "'";
		}
		// 数字
		if (obj instanceof Number) {
			return obj.toString();
		}
		// 布尔
		if (obj instanceof Boolean) {
			Boolean bl  = (Boolean)obj;
			if (bl) {
				return "1";
			} else {
				return "0";
			}
		}
		// 时间日期
		if (obj instanceof Date) {
			return "'" + DateUtil.date2Str((Date)obj, "yyyy-MM-dd HH:mm:ss") + "'";
		}
		return "'" + obj.toString() + "'";
	}
	
	@Override
	public String toString() {
		return "Sql [sqlStr=" + sqlStr + "]";
	}
	
}
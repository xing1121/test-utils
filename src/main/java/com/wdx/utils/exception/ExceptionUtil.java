package com.wdx.utils.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 描述：异常工具类
 * @author 80002888
 * @date   2018年9月25日
 */
public class ExceptionUtil {
	
	/**
	 * 获取异常的堆栈信息字符串
	 *	@ReturnType	String 
	 *	@Date	2018年1月3日	下午3:06:49
	 *  @Param  @param e
	 *  @Param  @return
	 */
    public static String getPrintStackTrace(Throwable e) {
    	if (e == null) {
			return null;
		}
    	StringWriter sw = new StringWriter();
    	PrintWriter pw = new PrintWriter(sw, true);
    	e.printStackTrace(pw);
    	if (sw.getBuffer() == null) {
			return null;
		}
    	return sw.getBuffer().toString();
    }
    
}

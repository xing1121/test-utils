package com.wdx.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DrawPicUtil {
	
	/**
	 * 画个圆
	 *	@ReturnType	void 
	 *	@Date	2018年1月15日	下午5:45:52
	 *  @Param  @param clazz
	 */
	public static void drawCircle(Class<?> clazz) {
		Logger logger = LoggerFactory.getLogger(clazz);
        int r = 50;  //半径
        StringBuffer path = new StringBuffer("##########################draw a circle##########################\n");
        for (int y = 0; y <= 2 * r; y += 2) {//y的步长为2,改变y的步长可以将圆形变成椭圆  
            int x = (int)Math.round(r - Math.sqrt(2 * r * y - y * y));  
            int len = 2 * (r - x);  
  
            for (int i = 0; i <= x; i++) {  
            	path.append(" ");  
            }     
            path.append("*");   
  
            for (int j = 0; j <= len; j++) {
            	  path.append(" ");   
            }  
            path.append("*");
            path.append(" \n");   
        } 
        logger.info(path.toString());
	}
	
}

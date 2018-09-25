package com.wdx.utils.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 描述：画图工具类
 * @author 80002888
 * @date   2018年9月25日
 */
public class DrawPicUtil {

	/**
	 * 半径
	 */
	private static final int RADIUS = 30;

	public static void main(String[] args) {
		drawStar(DrawPicUtil.class);
		drawCircle(DrawPicUtil.class);
	}
	
	/**
	 * 画星星
	 *	@ReturnType	void 
	 *	@Date	2018年9月25日	下午7:25:21
	 *  @Param  @param clazz
	 */
	public static void drawStar(Class<?> clazz) {
		Logger logger = LoggerFactory.getLogger(clazz);
		StringBuffer path = new StringBuffer("##########################draw a circle##########################\n");
		path.append("******************************************************************************\n");
		path.append("*                  *                                                         *\n");
		path.append("*                 * *                                                        *\n");
		path.append("*                *   *                                                       *\n");
		path.append("*               *     *                                                      *\n");
		path.append("*      * * * * *       * * * * *                                             *\n");
		path.append("*         *                 *                                                *\n");
		path.append("*           *             *                                                  *\n");
		path.append("*            *           *                                                   *\n");
		path.append("*           *      *      *                                                  *\n");
		path.append("*          *   *       *   *                                                 *\n");
		path.append("*         * *             * *                                                *\n");
		path.append("*                                                                            *\n");
		path.append("******************************************************************************");
		logger.info(path.toString());
	}

	/**
	 * 画圆
	 *	@ReturnType	void 
	 *	@Date	2018年9月25日	下午7:25:28
	 *  @Param  @param clazz
	 */
	public static void drawCircle(Class<?> clazz) {
		Logger logger = LoggerFactory.getLogger(clazz);
		StringBuffer path = new StringBuffer("##########################draw a circle##########################\n");
		// y的步长为2,改变y的步长可以将圆形变成椭圆
		for (int y = 0; y <= 2 * RADIUS; y += 2) {
			int x = (int) Math.round(RADIUS - Math.sqrt(2 * RADIUS * y - y * y));
			int len = 2 * (RADIUS - x);

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

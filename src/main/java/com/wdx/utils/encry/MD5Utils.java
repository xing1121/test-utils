package com.wdx.utils.encry;

import java.security.MessageDigest;
import java.util.Random;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 描述：MD5加密工具类
			MD5算法 哈希算法
			MD5算法具有以下特点：
				1、压缩性：任意长度的数据，算出的MD5值长度都是固定的。
				2、容易计算：从原数据计算出MD5值很容易。
				3、抗修改性：对原数据进行任何改动，哪怕只修改1个字节，所得到的MD5值都有很大区别。
				4、强抗碰撞：已知原数据和其MD5值，想找到一个具有相同MD5值的数据（即伪造数据）是非常困难的。
 * @author 80002888
 * @date   2018年9月25日
 */
public class MD5Utils {
	
	private static Logger logger = LoggerFactory.getLogger(DESUtils.class);
	
	public static void main(String args[]) {
		String plaintext = new String("admin");
		
		System.out.println("原始：" + plaintext);
		System.out.println("普通MD5后：" + digest(plaintext));
		System.out.println("二次加密后：" + digest(digest(plaintext)));
		
		// 获取加盐后的MD5值
		String ciphertext = generate(plaintext);
		System.out.println("加盐后MD5：" + ciphertext);
		System.out.println("是否是同一字符串:" + verify(plaintext, ciphertext));
		
		// 其中某次admin字符串的MD5值
		String[] tempSalt = { "990d7310af91261f7e366045319f1990d86b15387d69ca57", "b42d96e86740f45a7079fa25159312b27b2337fd36e51b04", "172680a8e455f89e6f92ec1c82d023250011a94c30f0fa04" };
		for (String temp : tempSalt) {
			System.out.println("是否是同一字符串:" + verify(plaintext, temp));
		}
		
	}
	
	/**
	 * 加盐加密（32位）
	 *	@ReturnType	String 
	 *	@Date	2018年9月20日	下午6:28:42
	 *  @Param  @param data
	 *  @Param  @param salt
	 *  @Param  @return
	 */
	public static String digest(String data, String salt) {
		if (StringUtils.isBlank(salt)) {
			return digest(data);
		}
		return digest(data + salt);
	}
	
	/**
	 * 32位加密（默认）
	 *	@ReturnType	String 
	 *	@Date	2018年9月20日	下午6:28:42
	 *  @Param  @param data
	 *  @Param  @return
	 */
	public static String digest(String data) {
		return digest(data, 32);
	}
	
	/**
	 * 16位加密
	 *	@ReturnType	String 
	 *	@Date	2018年9月20日	下午6:28:13
	 *  @Param  @param data
	 *  @Param  @return
	 */
	public static String digest16(String data) {
		return digest(data, 16);
	}

	/**
	 * 加密
	 *	@ReturnType	String 
	 *	@Date	2018年9月20日	下午6:27:57
	 *  @Param  @param data	明文
	 *  @Param  @param rang	位数
	 *  @Param  @return
	 */
	private static String digest(String data, int rang) {
		try {
			MessageDigest md5 = null;
			if (StringUtils.isEmpty(data)) {
				return null;
			}
			md5 = MessageDigest.getInstance("MD5");
			char[] charArray = data.toCharArray();
			byte[] byteArray = new byte[charArray.length];

			for (int i = 0; i < charArray.length; i++) {
				byteArray[i] = (byte) charArray[i];
			}
			// 转换为字节数组，长度一定16，如[33, 35, 47, 41, 122, 87, -91, -89, 67, -119, 74, 14, 74, -128, 31, -61]
			byte[] md5Bytes = md5.digest(byteArray);
			// 数组中每个元素转为十六进制字符串，并拼接到一起，当这个元素<16时（只有一位）前面填个0（如：14->0e）
			StringBuilder hexValue = new StringBuilder();
			for (int i = 0; i < md5Bytes.length; i++) {
				int val = ((int) md5Bytes[i]) & 0xff;
				if (val < 16){
					hexValue.append("0");
				}
				hexValue.append(Integer.toHexString(val));
			}
			if (rang == 32) {
				return hexValue.toString();
			} else {
				return hexValue.toString().substring(8, 24);
			}
		} catch (Exception e) {
			logger.error("get error->", e);
		}
		return null;
	}
	
	/**
	 * 加盐MD5加密
	 *	@ReturnType	String 
	 *	@Date	2018年9月20日	下午6:51:42
	 *  @Param  @param password
	 *  @Param  @return
	 */
	public static String generate(String password) {
		Random r = new Random();
 		StringBuilder sb = new StringBuilder(16);
 		sb.append(r.nextInt(99999999)).append(r.nextInt(99999999));
 		int len = sb.length();
 		if (len < 16) {
 			for (int i = 0; i < 16 - len; i++) {
 				sb.append("0");
 			}
 		}
 		String salt = sb.toString();
 		password = md5Hex(password + salt);
 		char[] cs = new char[48];
 		for (int i = 0; i < 48; i += 3) {
 			cs[i] = password.charAt(i / 3 * 2);
 			char c = salt.charAt(i / 3);
 			cs[i + 1] = c;
 			cs[i + 2] = password.charAt(i / 3 * 2 + 1);
 		}
		return new String(cs);
	}

	/**
	 * 校验加盐后是否和原文一致
	 * @author daniel
	 * @time 2016-6-11 下午8:45:39
	 * @param password
	 * @param md5
	 * @return
	 */
	public static boolean verify(String password, String md5) {
 		char[] cs1 = new char[32];
		char[] cs2 = new char[16];
		for (int i = 0; i < 48; i += 3) {
			cs1[i / 3 * 2] = md5.charAt(i);
			cs1[i / 3 * 2 + 1] = md5.charAt(i + 2);
			cs2[i / 3] = md5.charAt(i + 1);
		}
		String salt = new String(cs2);
		return md5Hex(password + salt).equals(new String(cs1));
	}
	/**
	 * 获取十六进制字符串形式的MD5摘要
	 */
	private static String md5Hex(String src) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			byte[] bs = md5.digest(src.getBytes());
			return new String(new Hex().encode(bs));
		} catch (Exception e) {
			return null;
		}
	}
	
}
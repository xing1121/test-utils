package com.wdx.utils.encry;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * 描述：AES加密工具类
 * @author 80002888
 * @date   2018年9月20日
 */
public class AesUtil {
	
	private static Logger logger = LoggerFactory.getLogger(AesUtil.class);

	/**
	 * 秘钥
	 */
	public static final String KEY = "2D2FF77D67ECD398";

	public static void main(String[] args) {
		String str = " 嗯  !1";
		String encrypt = encrypt(str, KEY);

		System.out.println("加密前：" + str);
		System.out.println("加密后：" + encrypt);
		System.out.println("解密后：" + decrypt(encrypt, KEY));
	}

	/**
	 * AES加密
	 *	@ReturnType	String 
	 *	@Date	2018年9月20日	下午5:42:35
	 *  @Param  @param data	明文
	 *  @Param  @return
	 */
	public static String encrypt(String data, String key) {
		try {
			if (data == null){
				return null;
			}
			if (StringUtils.isBlank(key)) {
				key = KEY;
			}
			byte[] raw = key.getBytes();
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			// 算法/模式/补码方式
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
			IvParameterSpec iv = new IvParameterSpec(key.getBytes());
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
			byte[] encrypted = cipher.doFinal(data.getBytes());
			// 此处再使用BAES64做转码功能，同时能起到2次加密的作用
			return new BASE64Encoder().encode(encrypted);
		} catch (Exception e) {
			logger.error("get error->" + e.getMessage(), e);
		}
		return null;
	}

	/**
	 * AES解密
	 *	@ReturnType	String 
	 *	@Date	2018年9月20日	下午5:42:48
	 *  @Param  @param data	密文
	 *  @Param  @return
	 */
	public static String decrypt(String data, String key) {
		try {
			if (data == null){
				return null;
			}
			if (StringUtils.isBlank(key)) {
				key = KEY;
			}
			// 初始化密码器
			byte[] raw = key.getBytes("ASCII");
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			IvParameterSpec iv = new IvParameterSpec(key.getBytes());
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			// 先用BASE64解密，再用AES解密
			byte[] encrypted1 = new BASE64Decoder().decodeBuffer(data);
			byte[] original = cipher.doFinal(encrypted1);
			return new String(original);
		} catch (Exception e) {
			logger.error("get error->" + e.getMessage(), e);
		}
		return null;
	}

}

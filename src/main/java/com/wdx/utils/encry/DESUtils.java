package com.wdx.utils.encry;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * 描述：DES加密工具类
 * @author 80002888
 * @date   2018年9月20日
 */
public class DESUtils {

	private static Logger logger = LoggerFactory.getLogger(DESUtils.class);
	
	/**
	 * 秘钥
	 */
	public static final String KEY = "2D2FF77D67ECD398";

	public static void main(String[] args) throws Exception {
		String str = " 嗯  !1";
		String encrypt = encrypt(str, KEY);

		System.out.println("加密前：" + str);
		System.out.println("加密后：" + encrypt);
		System.out.println("解密后：" + decrypt(encrypt, KEY));
	}

	/**
	 * DES加密
	 *	@ReturnType	String 
	 *	@Date	2018年9月20日	下午5:43:33
	 *  @Param  @param data	明文
	 *  @Param  @param key	秘钥
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
			byte[] bt = encrypt(data.getBytes(), key.getBytes());
			return new BASE64Encoder().encode(bt);
		} catch (Exception e) {
			logger.error("get error->" + e.getMessage(), e);
		}
		return null;
	}

	/**
	 * DES解密
	 *	@ReturnType	String 
	 *	@Date	2018年9月20日	下午5:43:48
	 *  @Param  @param data	密文
	 *  @Param  @param key	秘钥
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
			BASE64Decoder decoder = new BASE64Decoder();
			byte[] buf = decoder.decodeBuffer(data);
			byte[] bt = decrypt(buf, key.getBytes());
			return new String(bt);
		} catch (Exception e) {
			logger.error("get error->" + e.getMessage(), e);
		}
		return null;
	}

	/****************************	private	*********************************/
	
	/**
	 * DES加密
	 *	@ReturnType	byte[] 
	 *	@Date	2018年9月20日	下午5:53:41
	 *  @Param  @param data
	 *  @Param  @param key
	 *  @Param  @return
	 *  @Param  @throws Exception
	 */
	private static byte[] encrypt(byte[] data, byte[] key) throws Exception {
		// 生成一个可信任的随机数源
		SecureRandom sr = new SecureRandom();
		// 从原始密钥数据创建DESKeySpec对象
		DESKeySpec dks = new DESKeySpec(key);
		// 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey securekey = keyFactory.generateSecret(dks);
		// Cipher对象实际完成加密操作
		Cipher cipher = Cipher.getInstance("DES");
		// 用密钥初始化Cipher对象
		cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);
		return cipher.doFinal(data);
	}

	/**
	 * DES解密
	 *	@ReturnType	byte[] 
	 *	@Date	2018年9月20日	下午5:53:51
	 *  @Param  @param data
	 *  @Param  @param key
	 *  @Param  @return
	 *  @Param  @throws Exception
	 */
	private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
		// 生成一个可信任的随机数源
		SecureRandom sr = new SecureRandom();
		// 从原始密钥数据创建DESKeySpec对象
		DESKeySpec dks = new DESKeySpec(key);
		// 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey securekey = keyFactory.generateSecret(dks);
		// Cipher对象实际完成解密操作
		Cipher cipher = Cipher.getInstance("DES");
		// 用密钥初始化Cipher对象
		cipher.init(Cipher.DECRYPT_MODE, securekey, sr);
		return cipher.doFinal(data);
	}
}

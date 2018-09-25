package com.wdx.utils.encry.no2;

import java.security.Key;

import org.apache.shiro.codec.Base64;
import org.apache.shiro.codec.Hex;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.junit.Test;

/**
 * 描述：测试Shiro加密
 * @author 80002888
 * @date   2018年9月25日
 */
public class ShiroEncryptTest {
	
	/**
	 * 对称加密解密；算了。。。
	 * 非对称目前没有得到支持；
	 */
	@Test
	public void testAes(){
		AesCipherService aesCipherService = new AesCipherService();
		
		//设置key长度;128 192 256
		aesCipherService.setKeySize(128);
		
		//生成key
		Key key = aesCipherService.generateNewKey();
		String text = "admin";
		
		//加密
		String string = aesCipherService.encrypt(text.getBytes(), key.getEncoded()).toString();
		System.out.println("加密："+string);
		
		//解密
		String text2 =  aesCipherService.decrypt(string.getBytes(), key.getEncoded()).toString();
		System.out.println("解密："+text2);
	}
	
	/**
	 * 测试散列算法
	 * 1、散列算法一般用于生成数据的摘要信息，是一种不可逆的算法，一般适合存储密码之类的数据。
	 * 2、常见的散列算法如MD5、SHA等。
	 * 3、一般进行散列时最好提供一个salt（盐），
	 * 比如加密密码“admin”，产生的散列值是“21232f297a57a5a743894a0e4a801fc3”，
	 * 可以到一些md5解密网站很容易的通过散列值得到密码“admin”，
	 * 即如果直接对密码进行散列相对来说破解更容易，
	 * 此时我们可以加一些只有系统知道的干扰数据，如用户名和ID（即盐）；
	 * 这样散列的对象是“密码+用户名+ID”，这样生成的散列值相对来说更难破解。
	 */
	@Test
	public void testHash(){
		//MD5 盐加密
		String str = "admin";
		Md5Hash hash = new Md5Hash(str, str, 1);
		System.out.println(hash.toString());
		
		//使用SHA256算法生成相应的散列数据，另外还有如SHA1、SHA512算法
		Sha256Hash sha256Hash = new Sha256Hash(str, str, 2);
		System.out.println(sha256Hash.toString());
		
		//org.apache.shiro.crypto.hash测试其他散列加密算法
	}
	
	/**
	 * 测试16进制编解码
	 */
	@Test
	public void testHex(){
		String str = "zhangsan123";
		String encode = Hex.encodeToString(str.getBytes());
		System.out.println("16进制编码："+encode);
		byte[] decode = Hex.decode(encode);
		System.out.println("16进制解码："+new String(decode));
	}
	
	/**
	 * 测试Base64编码和解码
	 */
	@Test
	public void testBase64(){
		String str = "zhangsan123";
		String encode = Base64.encodeToString(str.getBytes());
		System.out.println("编码后的："+encode);
		String decode = Base64.decodeToString(encode);
		System.out.println("解码后的："+decode);
	}

}

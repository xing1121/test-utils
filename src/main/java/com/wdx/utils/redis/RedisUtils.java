package com.wdx.utils.redis;

import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 描述：单台redis工具类
 * @author 80002888
 * @date   2018年9月20日
 */
public class RedisUtils {

	private static JedisPool jedisPool;

	public static void main(String[] args) {
		String key = "enne";
		RedisUtils.set(key, "我哈哈");
		System.out.println(RedisUtils.get(key));
	}

	/**
	 * 初始化
	 */
	static {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(100);
		config.setMaxIdle(10);
		config.setMaxWaitMillis(100);
		config.setTestOnBorrow(true);
		config.setTestOnReturn(true);

		String host = "127.0.0.1";
		int port = 6379;
		String password = "123456";
		int timeout = (30 * 60 * 1000);

		jedisPool = new JedisPool(config, host, port, timeout, password);
	}

	public static void set(byte[] key, byte[] value) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			jedis.set(key, value);
		} finally {
			jedis.close();
		}
	};

	public static void set(String key, String value) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			jedis.set(key, value);
		} finally {
			jedis.close();
		}
	};

	public static byte[] get(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			return jedis.get(key);
		} finally {
			jedis.close();
		}
	}

	public static String get(String key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			return jedis.get(key);
		} finally {
			jedis.close();
		}
	}

	public static void expire(byte[] key, int seconds) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			jedis.expire(key, seconds);
		} finally {
			jedis.close();
		}
	}

	public static void expire(String key, int seconds) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			jedis.expire(key, seconds);
		} finally {
			jedis.close();
		}
	}

	public static void del(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			jedis.del(key);
		} finally {
			jedis.close();
		}
	}

	public static void del(String key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			jedis.del(key);
		} finally {
			jedis.close();
		}
	}

	public static Set<byte[]> keys(byte[] pattern) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			return jedis.keys(pattern);
		} finally {
			jedis.close();
		}
	}

	public static Set<String> keys(String pattern) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			return jedis.keys(pattern);
		} finally {
			jedis.close();
		}
	}

}
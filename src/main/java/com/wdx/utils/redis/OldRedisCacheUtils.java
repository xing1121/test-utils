package com.wdx.utils.redis;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wdx.utils.collection.CollectionUtils;
import com.wdx.utils.file.PropertiesUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

/**
 * 描述：RedisUtil
 * @author 80002888
 * @date   2018年6月21日
 */
public class OldRedisCacheUtils {

	private static final Logger logger = LoggerFactory.getLogger(OldRedisCacheUtils.class);

	private static Map<String, JedisSentinelPool> poolMap;

	/**
	 * 默认为配置中最后一个redis服务器
	 */
	private static final JedisSentinelPool DEFAULT_POOL;
	
	private static String[] mastersArr;
	
	private static String LOCK = "lock_";
	
	/**
	 * 初始化
	 */
	static {
		JedisPoolConfig config = new JedisPoolConfig();
		// 连接池中连接最大数量
		config.setMaxTotal(200);
		// 最大空闲连接数量
		config.setMaxIdle(100);
		// 获取连接最长等待时间ms
		config.setMaxWaitMillis(100);
		config.setTestOnBorrow(true);
		config.setTestOnReturn(true);
		config.setTestWhileIdle(true);

		String jpassword = PropertiesUtils.getValue("/config/application.properties", "redis_password");
		
		//超时时间（单位毫秒，2.8以下即是连接超时时间，又是读写超时时间）
		int timeout = Integer.parseInt(PropertiesUtils.getValue("/config/application.properties", "redis_time"));	

		String masters = PropertiesUtils.getValue("/config/application.properties", "redis_masters");
		mastersArr = masters.split(",");
		int size = mastersArr.length;
		
		Set<String> sentinels = new HashSet<String>();
		String sentinelsd = PropertiesUtils.getValue("/config/application.properties", "redis_servers");
		String[] sentinelsArr = sentinelsd.split(",");
		sentinels.addAll(Arrays.asList(sentinelsArr));
		
		poolMap = new HashMap<>();
		for (int i = 0; i < size; i++) {
			String masterName = mastersArr[i];
			poolMap.put(masterName, new JedisSentinelPool(masterName, sentinels, config, timeout, jpassword));
		}

		DEFAULT_POOL = new JedisSentinelPool(mastersArr[size-1], sentinels, config, timeout, jpassword);
	}

	/**
	 * 从redis获取数据
	 *	@ReturnType	String 
	 *	@Date	2018年1月23日	上午11:37:00
	 *  @Param  @param key
	 *  @Param  @return
	 */
	public static String get(String key) {
		String value = null;
		Jedis jedis = null;
		JedisSentinelPool pool = getPool(key);
		try {
			if (pool == null) {
				pool = DEFAULT_POOL;
			}
			jedis = pool.getResource();
			value = jedis.get(key);
		} catch (Exception e) {
			logger.error("redis get ===>", e);
		} finally {
			closeJedis(jedis);
		}
		return value;
	}
	
	/**
	 * 从redis获取数据
	 *	@ReturnType	String 
	 *	@Date	2018年1月23日	上午11:37:00
	 *  @Param  @param key
	 *  @Param  @param defaultValue		默认值
	 *  @Param  @return
	 */
	public static String get(String key, String defaultValue) {
		String value = null;
		Jedis jedis = null;
		JedisSentinelPool pool = getPool(key);
		try {
			if (pool == null) {
				pool = DEFAULT_POOL;
			}
			jedis = pool.getResource();
			value = jedis.get(key);
		} catch (Exception e) {
			logger.error("redis get ===>", e);
		} finally {
			closeJedis(jedis);
		}
		if (StringUtils.isBlank(value)) {
			return defaultValue;
		}
		return value;
	}
	
	/**
	 * 从redis获取数据旧的值，并设置新的值。
	 *	@ReturnType	String 
	 *	@Date	2018年1月23日	上午11:37:00
	 *  @Param  @param key
	 *  @Param  @param value
	 *  @Param  @return
	 */
	public static String getSet(String key, String value) {
		String oldValue = null;
		Jedis jedis = null;
		JedisSentinelPool pool = getPool(key);
		try {
			if (pool == null) {
				pool = DEFAULT_POOL;
			}
			jedis = pool.getResource();
			oldValue = jedis.getSet(key, value);
		} catch (Exception e) {
			logger.error("redis get ===>", e);
		} finally {
			closeJedis(jedis);
		}
		return oldValue;
	}
	
	/**
	 * 从redis获取数据旧的值，并设置新的值，和生存时间
	 *	@ReturnType	String 
	 *	@Date	2018年1月23日	上午11:37:00
	 *  @Param  @param key
	 *  @Param  @param value
	 *  @Param  @param seconds
	 *  @Param  @return
	 */
	public static String getSet(String key, String value, int seconds) {
		String oldValue = null;
		Jedis jedis = null;
		JedisSentinelPool pool = getPool(key);
		try {
			if (pool == null) {
				pool = DEFAULT_POOL;
			}
			jedis = pool.getResource();
			oldValue = jedis.getSet(key, value);
			jedis.expire(key, seconds);
		} catch (Exception e) {
			logger.error("redis get ===>", e);
		} finally {
			closeJedis(jedis);
		}
		return oldValue;
	}

	/**
	 * 向redis设置数据，默认一个月
	 *	@ReturnType	void 
	 *	@Date	2018年1月23日	上午11:36:50
	 *  @Param  @param key
	 *  @Param  @param value
	 */
	public static void set(String key, String value) {
		setOneMonth(key, value);
	}
	
	/**
	 * 向redis设置数据
	 *	@ReturnType	void 
	 *	@Date	2018年1月23日	上午11:45:09
	 *  @Param  @param key
	 *  @Param  @param value
	 *  @Param  @param seconds	存活的秒数
	 */
	public static void set(String key, String value, int seconds) {
		Jedis jedis = null;
		JedisSentinelPool pool = getPool(key);
		try {
			if (pool == null) {
				pool = DEFAULT_POOL;
			}
			jedis = pool.getResource();
			value = jedis.setex(key, seconds, value);
		} catch (Exception e) {
			logger.error("redis set ===>", e);
		} finally {
			closeJedis(jedis);
		}
		return;
	}
	
	/**
	 * 永久存储
	 *	@ReturnType	void 
	 *	@Date	2018年1月23日	上午11:45:09
	 *  @Param  @param key
	 *  @Param  @param value
	 */
	public static void setForever(String key, String value) {
		set(key, value, (60*60*24*30*12*100));
	}
	
	/**
	 * 存储一个月
	 *	@ReturnType	void 
	 *	@Date	2018年1月23日	上午11:45:09
	 *  @Param  @param key
	 *  @Param  @param value
	 */
	public static void setOneMonth(String key, String value) {
		set(key, value, (60*60*24*30));
	}
	
	/**
	 * 如果不存在就设置进去
	 *	@ReturnType	boolean 
	 *	@Date	2018年9月21日	下午5:51:50
	 *  @Param  @param key
	 *  @Param  @param value
	 *  @Param  @param seconds
	 *  @Param  @return
	 */
	public static boolean setnx(String key, String value, int seconds){
		Jedis jedis = null;
		JedisSentinelPool pool = getPool(key);
		try {
			if (pool == null) {
				pool = DEFAULT_POOL;
			}
			jedis = pool.getResource();
			Long res = jedis.setnx(key, value);
			if (res == 1L) {
				jedis.expire(key, seconds);
				return true;
			}
		} catch (Exception e) {
			logger.error("redis set ===>", e);
		} finally {
			closeJedis(jedis);
		}
		return false;
	}
	
	/**
	 * 设置过期时间
	 *	@ReturnType	void 
	 *	@Date	2018年1月23日	上午11:45:58
	 *  @Param  @param key
	 *  @Param  @param seconds
	 */
	public static void expire(String key, int seconds) {
		Jedis jedis = null;
		JedisSentinelPool pool = getPool(key);
		try {
			if (pool == null) {
				pool = DEFAULT_POOL;
			}
			jedis = pool.getResource();
			jedis.expire(key, seconds);
		} catch (Exception e) {
			logger.error("redis set ===>", e);
		} finally {
			closeJedis(jedis);
		}
		return;
	}
	
	/**
	 * 是否存在key
	 *	@ReturnType	boolean 
	 *	@Date	2018年1月23日	上午11:46:41
	 *  @Param  @param key
	 *  @Param  @return
	 */
	public static boolean exists(String key){
		Jedis jedis = null;
		JedisSentinelPool pool = getPool(key);
		try {
			if (pool == null) {
				pool = DEFAULT_POOL;
			}
			jedis = pool.getResource();
			return jedis.exists(key);
		} catch (Exception e) {
			logger.error("redis set ===>", e);
		} finally {
			closeJedis(jedis);
		}
		return false;
	}
	
	/**
	 * 查看数据的生存时间
	 *	@ReturnType	Long 
	 *	@Date	2018年1月23日	上午11:37:00
	 *  @Param  @param key
	 *  @Param  @return		单位是秒，-2为数据不存在，-1为永不过期
	 */
	public static Long ttl(String key) {
		Long seconds = null;
		Jedis jedis = null;
		JedisSentinelPool pool = getPool(key);
		try {
			if (pool == null) {
				pool = DEFAULT_POOL;
			}
			jedis = pool.getResource();
			seconds = jedis.ttl(key);
		} catch (Exception e) {
			logger.error("redis ttl ===>", e);
		} finally {
			closeJedis(jedis);
		}
		return seconds;
	}
	
	/**
	 * 从redis获取keys
	 *	@ReturnType	String 
	 *	@Date	2018年1月23日	上午11:37:00
	 *  @Param  @param pattern			含通配符的字符串
	 *  @Param  @return
	 */
	public static Set<String> getKeys(String pattern) {
		Set<String> keys = new HashSet<>();
		Collection<JedisSentinelPool> pools = poolMap.values();
		if (CollectionUtils.isEmpty(pools)) {
			return null;
		}
		for (JedisSentinelPool pool : pools) {
			Collection<String> keysFromOnePool = getKeysFromOnePool(pattern, pool);
			if (CollectionUtils.isNotEmpty(keysFromOnePool)) {
				keys.addAll(keysFromOnePool);
			}
		}
		if (CollectionUtils.isEmpty(keys)) {
			return null;
		}
		return keys;
	}
	
	/**
	 * 按key移除
	 *	@ReturnType	void 
	 *	@Date	2018年1月23日	上午11:36:25
	 *  @Param  @param key
	 */
	public static long remove(String key) {
		Long res = null;
		Jedis jedis = null;
		JedisSentinelPool pool = getPool(key);
		try {
			jedis = pool.getResource();
			res = jedis.del(key);
		} catch (Exception e) {
			logger.error("redis del ===> ", e);
		} finally {
			closeJedis(jedis);
		}
		return res == null ? 0 : res;
	}

	/**
	 * 向redis批量设置数据，默认30天
	 *	@ReturnType	void 
	 *	@Date	2018年1月23日	上午11:45:09
	 *  @Param  @param map
	 */
	public static void setBatch(Map<String, String> datas) {
		setBatch(datas, (60*60*24*30));
	}
	
	/**
	 * 向redis批量设置数据
	 *	@ReturnType	void 
	 *	@Date	2018年1月23日	上午11:45:09
	 *  @Param  @param map
	 *  @Param  @param seconds
	 */
	public static void setBatch(Map<String, String> datas, int seconds) {
		if (datas == null || datas.size() == 0 || CollectionUtils.isEmpty(datas.keySet())) {
			return;
		}
		Map<JedisSentinelPool, List<String>> map = datas.keySet().stream().filter((x)->StringUtils.isNotBlank(x)).collect(Collectors.groupingBy(OldRedisCacheUtils::getPool));
		for (JedisSentinelPool pool : map.keySet()) {
			Jedis jedis = null;
			try {
				jedis = pool.getResource();
				List<String> keys = map.get(pool);
				if (CollectionUtils.isEmpty(keys)) {
					continue;
				}
				for (String key : keys) {
					if (StringUtils.isBlank(key)) {
						continue;
					}
					String value = datas.get(key);
					if (StringUtils.isBlank(value)) {
						continue;
					}
					jedis.setex(key, seconds, value);
				}
			} catch (Exception e) {
				logger.error("redis setBatch ===> ", e);
			} finally {
				closeJedis(jedis);
			}
		}
	}
	
	/**
	 * 按keys批量移除
	 *	@ReturnType	long 
	 *	@Date	2018年1月30日	下午7:13:12
	 *  @Param  @return		删除的数量
	 */
	public static long removeBatch(List<String> keys){
		if (CollectionUtils.isEmpty(keys)) {
			return 0;
		}
		int total = 0;
		Map<JedisSentinelPool, List<String>> map = keys.stream().filter(StringUtils::isNotBlank).collect(Collectors.groupingBy(OldRedisCacheUtils::getPool));
		for (JedisSentinelPool pool : map.keySet()) {
			Jedis jedis = null;
			try {
				jedis = pool.getResource();
				List<String> list = map.get(pool);
				if (CollectionUtils.isEmpty(list)) {
					continue;
				}
				total += jedis.del(CollectionUtils.listToArray(list));
			} catch (Exception e) {
				logger.error("redis del batch ===> ", e);
			} finally {
				closeJedis(jedis);
			}
		}
		return total;
	}
	
	/**
	 * 按keys批量获取
	 *	@ReturnType	String 
	 *	@Date	2018年1月30日	下午8:02:59
	 *  @Param  @param keys
	 *  @Param  @return
	 */
	public static List<String> getBatch(List<String> keys){
		if (CollectionUtils.isEmpty(keys)) {
			return null;
		}
		List<String> totalValues = new ArrayList<>();
		Map<JedisSentinelPool, List<String>> map = keys.stream().filter(StringUtils::isNotBlank).collect(Collectors.groupingBy(OldRedisCacheUtils::getPool));
		for (JedisSentinelPool pool : map.keySet()) {
			Jedis jedis = null;
			try {
				jedis = pool.getResource();
				List<String> list = map.get(pool);
				if (CollectionUtils.isEmpty(list)) {
					continue;
				}
				List<String> values = jedis.mget(CollectionUtils.listToArray(list));
				if (CollectionUtils.isEmpty(values)) {
					continue;
				}
				totalValues.addAll(values);
			} catch (Exception e) {
				logger.error("redis getBatch ===> ", e);
			} finally {
				closeJedis(jedis);
			}
		}
		if (CollectionUtils.isEmpty(totalValues)) {
			return null;
		}
		return totalValues;
	}
	
	/**
	 * 订阅频道
	 *	@ReturnType	void 
	 *	@Date	2018年2月3日	下午3:23:32
	 *  @Param  @param jedisPubSub
	 *  @Param  @param channel
	 */
	public static void subscribe(JedisPubSub jedisPubSub, String...channels){
		if(DEFAULT_POOL == null){
			return;
		}
		if (jedisPubSub == null) {
			return;
		}
		if (channels == null || channels.length == 0) {
			return;
		}
		Jedis jedis = null;
		try {
			jedis = DEFAULT_POOL.getResource();
			jedis.subscribe(jedisPubSub, channels);
		} catch (Exception e) {
			logger.error("redis subscribe ===> ", e);
		} finally {
			closeJedis(jedis);
		}
	}
	
	/**
	 * 向频道发布内容
	 *	@ReturnType	void 
	 *	@Date	2018年7月23日	下午2:05:27
	 *  @Param  @param channel
	 *  @Param  @param message
	 */
	public static void publish(String channel, String message){
		if(DEFAULT_POOL == null){
			return;
		}
		if (StringUtils.isBlank(channel) || StringUtils.isBlank(message)) {
			return;
		}
		Jedis jedis = null;
		try {
			jedis = DEFAULT_POOL.getResource();
			jedis.publish(channel, message);
		} catch (Exception e) {
			logger.error("redis publish ===> ", e);
		} finally {
			closeJedis(jedis);
		}
	}
	
	/**
	 * 向redis的set结构添加数据（添加在头部）
	 *	@ReturnType	void 
	 *	@Date	2018年8月22日	下午3:42:37
	 *  @Param  @param setName			set的名称
	 *  @Param  @param values			要添加的数据
	 */
	public static void lpush(String setName, String...values){
		Jedis jedis = null;
		JedisSentinelPool pool = getPool(setName);
		try {
			jedis = pool.getResource();
			jedis.lpush(setName, values);
		} catch (Exception e) {
			logger.error("redis lpush ===> ", e);
		} finally {
			closeJedis(jedis);
		}
		return;
	}
	
	/**
	 * 向redis的set结构添加数据（添加在尾部）
	 *	@ReturnType	void 
	 *	@Date	2018年8月22日	下午3:42:37
	 *  @Param  @param setName			set的名称
	 *  @Param  @param values			要添加的数据
	 */
	public static void rpush(String setName, String...values){
		Jedis jedis = null;
		JedisSentinelPool pool = getPool(setName);
		try {
			jedis = pool.getResource();
			jedis.rpush(setName, values);
		} catch (Exception e) {
			logger.error("redis rpush ===> ", e);
		} finally {
			closeJedis(jedis);
		}
		return;
	}
	
	/**
	 * 从redis的set结构弹出数据（从头部获取并删除）
	 *	@ReturnType	String 
	 *	@Date	2018年8月22日	下午3:50:59
	 *  @Param  @param setName				set的名称
	 *  @Param  @return						获取到的value
	 */
	public static String lpop(String setName){
		Jedis jedis = null;
		JedisSentinelPool pool = getPool(setName);
		try {
			jedis = pool.getResource();
			String value = jedis.lpop(setName);
			return value;
		} catch (Exception e) {
			logger.error("redis lpop ===> ", e);
		} finally {
			closeJedis(jedis);
		}
		return null;
	}
	
	/**
	 * 从redis的set结构弹出数据（从尾部获取并删除）
	 *	@ReturnType	void 
	 *	@Date	2018年8月22日	下午3:42:37
	 *  @Param  @param setName				set的名称
	 *  @Param  @return						获取到的value
	 */
	public static void rpop(String setName){
		Jedis jedis = null;
		JedisSentinelPool pool = getPool(setName);
		try {
			jedis = pool.getResource();
			jedis.rpop(setName);
		} catch (Exception e) {
			logger.error("redis rpop ===> ", e);
		} finally {
			closeJedis(jedis);
		}
		return;
	}
	
	/**
	 * 从redis的set结构弹出数据（阻塞从头部获取并删除）
	 *	@ReturnType	String 
	 *	@Date	2018年8月22日	下午3:50:59
	 *  @Param  @param timeout			超时时间（s）
	 *  @Param  @param setName			set的名称
	 *  @Param  @return					如果超时且取不到，则为null。
	 *  								如果取到了，list(0)为key，list(1)为value（一次只弹出一个元素）
	 */
	public static List<String> blpop(int timeout, String setName){
		Jedis jedis = null;
		JedisSentinelPool pool = getPool(setName);
		try {
			jedis = pool.getResource();
			List<String> resList = jedis.blpop(timeout, setName);
			return resList;
		} catch (Exception e) {
			logger.error("redis blpop ===> ", e);
		} finally {
			closeJedis(jedis);
		}
		return null;
	}
	
	/**
	 * 从redis的set结构弹出数据（阻塞从尾部获取并删除）
	 *	@ReturnType	String 
	 *	@Date	2018年8月22日	下午3:50:59
	 *  @Param  @param timeout			超时时间（s）
	 *  @Param  @param setName			set的名称
	 *  @Param  @return					如果超时且取不到，则为null。
	 *  								如果取到了，list(0)为key，list(1)为value（一次只弹出一个元素）
	 */
	public static List<String> brpop(int timeout, String setName){
		Jedis jedis = null;
		JedisSentinelPool pool = getPool(setName);
		try {
			jedis = pool.getResource();
			List<String> resList = jedis.brpop(timeout, setName);
			return resList;
		} catch (Exception e) {
			logger.error("redis brpop ===> ", e);
		} finally {
			closeJedis(jedis);
		}
		return null;
	}
	
	/**
	 * 使用redis实现分布式锁
	 *	@ReturnType	void 
	 *	@Date	2018年9月21日	下午6:00:50
	 *  @Param  @param key					数据的key
	 *  @Param  @param timeout				超时时间（超时未获得锁会报异常）、锁存活最长时间。默认60s
	 */
	public static void lock(String key, Integer timeout){
		if (key == null || "".equals(key.trim())) {
			return;
		}
		if (timeout == null || timeout <= 0) {
			timeout = 60;
		}
		// 初始时间
		Instant start = Instant.now();
		// 锁的key
		String lockKey = LOCK + key;
		try {
			// 是否获取锁成功
			boolean lockStatus = false;
			// 循环timeout秒
			while (Duration.between(start, Instant.now()).toMillis() <= 1000 * timeout) {
				logger.info("redis...lock...try get lock->" + lockKey + ", wait->" + (Duration.between(start, Instant.now()).toMillis()) / 1000 + "s, max->" + timeout + "s");
				// 锁的过期时间
				long expireTime = (System.currentTimeMillis() + (timeout * 1000));
				// 尝试上锁
				lockStatus = setnx(lockKey, String.valueOf(expireTime), timeout);
				// 上锁成功，跳出循环
				if (lockStatus) {
					break;
				}
				// 上锁失败，可能是锁被别的地方抢走，或者死锁（生存时间永久，这种需要以下判断时间）
				// 先获取，看是否存在这个锁，存在则获取之前的过期时间
				long oldExpireTime = Long.parseLong(get(lockKey, "0"));
				// 如果锁中的过期时间小于当前系统时间，超时，可以允许别的请求重新获取
				if (oldExpireTime < System.currentTimeMillis()) {
					// 计算新的过期时间
					long newExpireTime = (System.currentTimeMillis() + (timeout * 1000));
					// 尝试获取当前锁中的生存时间，并设置新的过期时间进去
					String v = getSet(lockKey, String.valueOf(newExpireTime), timeout);
					long currentExpireTime = Long.parseLong(StringUtils.isBlank(v)? "0" : v);
					// 如果当前锁中的生存时间和之前获取的一致，说明没有被人设置过
					if (currentExpireTime == oldExpireTime) {
						// 上锁成功
						lockStatus = true;
						// 跳出循环
						break;
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
			// 超时
			if (!lockStatus) {
				throw new RuntimeException("redis lock-> " + lockKey + " fail...timeout : " + timeout);
			}
			// 上锁成功
			logger.info("redis...lock...get lock success->" + lockKey);
		} catch (Exception e) {
			logger.error("redis...lock...get error->" + lockKey, e);
		}
	}
	
	/**
	 * redis释放锁
	 *	@ReturnType	void 
	 *	@Date	2018年9月21日	下午6:00:41
	 *  @Param  @param key				数据的key
	 */
	public static void unlock(String key){
		if (key == null || "".equals(key.trim())) {
			return;
		}
		// 锁的key
		String lockKey = LOCK + key;
		try {
			// 释放锁
			Long res = remove(lockKey);
			if (res == null || res == 0) {
				return;
			}
			logger.info("redis...unlock...unlock success->" + lockKey);
		} catch (Exception e) {
			logger.error("redis...unlock...get error->" + lockKey, e);
		}
	}
	
	/******************************************************************************/
	
	/**
	 * 根据key获取JedisSentinelPool
	 *	@ReturnType	JedisSentinelPool 
	 *	@Date	2018年10月15日	下午3:30:20
	 *  @Param  @param key
	 *  @Param  @return
	 */
	protected static JedisSentinelPool getPool(String key){
		if (StringUtils.isBlank(key)) {
			return DEFAULT_POOL;
		}
		// 按HashCode
		int index = Math.abs(key.hashCode() % mastersArr.length);
		String masterName = mastersArr[index];
		JedisSentinelPool pool = getPoolByMasterName(masterName);
		if (pool == null) {
			return DEFAULT_POOL;
		}
		return pool;
	}
	
	/**
	 * 根据主机名称获取 JedisSentinelPool
	 */
	protected static JedisSentinelPool getPoolByMasterName(String masterName){
		if (StringUtils.isBlank(masterName)) {
			return null;
		}
		JedisSentinelPool pool = poolMap.get(masterName);
		return pool;
	}
	
	/**
	 * 关闭jedis连接
	 */
	protected static void closeJedis(Jedis jedis) {
		if (jedis != null) {
			jedis.close();
		}
	}
	
	/**
	 * 根据通配符从指定redis服务器查询keys，使用scan命令代替jedis.keys(pattern)
	 */
	protected static Set<String> getKeysFromOnePool(String pattern, JedisSentinelPool pool){
		Jedis jedis = null;
		Set<String> keys = null;
		String startCursor = "0";
		try {
			jedis = pool.getResource();
			keys = new HashSet<>();
			String cursor = startCursor;
			Instant start = Instant.now();
			do {
				ScanResult<String> scanResult = jedis.scan(cursor, new ScanParams().count(1000).match(pattern));
				List<String> results = scanResult.getResult();
				cursor = scanResult.getStringCursor();
				if (CollectionUtils.isEmpty(results)) {
					continue;
				}
				keys.addAll(new HashSet<>(results));
			} while (!startCursor.equals(cursor) && (Duration.between(start, Instant.now()).toMillis()<=1000*60));
		} catch (Exception e) {
			logger.error("redis get ===>", e);
		} finally {
			closeJedis(jedis);
		}
		if (CollectionUtils.isEmpty(keys)) {
			return null;
		}
		return keys;
	}

	public static JedisSentinelPool getDefaultPool(){
		return DEFAULT_POOL;
	}
	
	/**
	 * 销毁连接池 
	 */
	public static void destroyPool(){
		poolMap.values().forEach((x)->x.destroy());
		DEFAULT_POOL.destroy();
	}
	
}
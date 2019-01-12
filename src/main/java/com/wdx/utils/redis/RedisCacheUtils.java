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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.wdx.utils.collection.CollectionUtils;
import com.wdx.utils.file.PropertiesUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

/**
 * 描述：RedisCacheUtils
 * @author 80002888
 * @date   2018年6月21日
 */
public class RedisCacheUtils {

	private static final Logger logger = LoggerFactory.getLogger(RedisCacheUtils.class);
	
	/**
	 * masterName=连接池
	 */
	private static Map<String, JedisSentinelPool> poolMap;

	/**
	 * 默认为配置中最后一个redis服务器
	 */
	private static final JedisSentinelPool DEFAULT_POOL;
	
	/**
	 * 主机数组
	 */
	private static String[] mastersArr;
	
	/**
	 * 统一强制锁前缀
	 */
	private static String LOCK = "lock_";
	
	/**
	 * 初始化
	 */
	static {
		// 配置对象
		JedisPoolConfig config = new JedisPoolConfig();
		// 连接池中连接最大数量
		config.setMaxTotal(PropertiesUtils.getIntValue("/config/application.properties", "redis.maxTotal", 500));
		// 最大空闲连接数量
		config.setMaxIdle(PropertiesUtils.getIntValue("/config/application.properties", "redis.maxIdle", 100));
		// 获取连接最长等待时间ms
		config.setMaxWaitMillis(PropertiesUtils.getIntValue("/config/application.properties", "redis.maxWaitMillis", 1000));
		config.setTestOnBorrow(PropertiesUtils.getBooleanValue("/config/application.properties", "redis.testOnBorrow", true));
		config.setTestOnReturn(PropertiesUtils.getBooleanValue("/config/application.properties", "redis.testOnReturn", true));
		config.setTestWhileIdle(PropertiesUtils.getBooleanValue("/config/application.properties", "redis.testWhileIdle", true));
		// 超时时间（单位毫秒，2.8以下即是连接超时时间，又是读写超时时间）
		int timeout = Integer.parseInt(PropertiesUtils.getValue("/config/application.properties", "redis.timeout"));
		// 密码
		String jpassword = PropertiesUtils.getValue("/config/application.properties", "redis.password");
		// 主机
		String masters = PropertiesUtils.getValue("/config/application.properties", "redis.masters");
		mastersArr = masters.split(",");
		int size = mastersArr.length;
		// 哨兵
		Set<String> sentinels = new HashSet<String>();
		String[] sentinelsArr = PropertiesUtils.getValue("/config/application.properties", "redis.sentinels").split(",");
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
	public static String get(String key) {// 请使用StmsRedisTemplate.get
		return execute(key, jedis->jedis.get(key));
	}
	
	/**
	 * 从redis获取数据
	 *	@ReturnType	String 
	 *	@Date	2018年1月23日	上午11:37:00
	 *  @Param  @param key
	 *  @Param  @param defaultValue		默认值
	 *  @Param  @return
	 */
	public static String get(String key, String defaultValue) {// 请使用StmsRedisTemplate.get
		String value = execute(key, jedis->jedis.get(key));
		return value == null ? defaultValue : value;
	}
	
	/**
	 * 从redis获取数据旧的值，并设置新的值。
	 *	@ReturnType	String 
	 *	@Date	2018年1月23日	上午11:37:00
	 *  @Param  @param key
	 *  @Param  @param value
	 *  @Param  @return
	 */
	public static String getSet(String key, String value) {// 请使用StmsRedisTemplate.getSet
		return execute(key, jedis->jedis.getSet(key, value));
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
	public static String getSet(String key, String value, int seconds) { // 请使用StmsRedisTemplate.getSet
		return execute(key, jedis->{
			String oldValue = jedis.getSet(key, value);
			jedis.expire(key, seconds);
			return oldValue;
		});
	}

	/**
	 * 向redis设置数据，默认一个月
	 *	@ReturnType	void 
	 *	@Date	2018年1月23日	上午11:36:50
	 *  @Param  @param key
	 *  @Param  @param value
	 */
	public static void set(String key, String value) {// 请使用StmsRedisTemplate
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
	public static void set(String key, String value, int seconds) { // 请使用StmsRedisTemplate
		execute(key, jedis->jedis.setex(key, seconds, value));
	}

	
	/**
	 * 存储一个月
	 *	@ReturnType	void 
	 *	@Date	2018年1月23日	上午11:45:09
	 *  @Param  @param key
	 *  @Param  @param value
	 */
	public static void setOneMonth(String key, String value) { // // 请使用 StmsJSONRedisTemplate
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
	public static boolean setnx(String key, String value, int seconds){ // 请使用StmsRedisTemplate
		return execute(key, jedis->{
			boolean succ = jedis.setnx(key, value)==1;
			if(succ){
				jedis.expire(key, seconds);
			}
			return succ;
		});
	}
	
	/**
	 * 设置过期时间
	 *	@ReturnType	void 
	 *	@Date	2018年1月23日	上午11:45:58
	 *  @Param  @param key
	 *  @Param  @param seconds
	 */
	public static void expire(String key, int seconds) {// 请使用StmsRedisTemplate
		execute(key, jedis->jedis.expire(key, seconds));
	}
	
	/**
	 * 是否存在key
	 *	@ReturnType	boolean 
	 *	@Date	2018年1月23日	上午11:46:41
	 *  @Param  @param key
	 *  @Param  @return
	 */
	public static boolean exists(String key){// 请使用StmsRedisTemplate
		return execute(key, jedis->jedis.exists(key));
	}
	
	/**
	 * 查看数据的生存时间
	 *	@ReturnType	Long 
	 *	@Date	2018年1月23日	上午11:37:00
	 *  @Param  @param key
	 *  @Param  @return		单位是秒，-2为数据不存在，-1为永不过期
	 */
	public static Long ttl(String key) {// 请使用StmsRedisTemplate
		return execute(key, jedis->jedis.ttl(key));
	}
	
	/**
	 * 从redis获取keys
	 *	@ReturnType	String 
	 *	@Date	2018年1月23日	上午11:37:00
	 *  @Param  @param pattern			含通配符的字符串
	 *  @Param  @return
	 */
	public static Set<String> getKeys(String pattern) {
		return executeBatchSet(jedis->{
			Set<String> keys = new HashSet<>();
			String startCursor = "0";
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
			} while (!startCursor.equals(cursor) && (Duration.between(start, Instant.now()).toMillis()<=1000*300));
			return keys;
		});
	}
	
	/**
	 * 按key移除
	 *	@ReturnType	void 
	 *	@Date	2018年1月23日	上午11:36:25
	 *  @Param  @param key
	 */
	public static long remove(String key) {// 请使用StmsRedisTemplate
		return execute(key, jedis->jedis.del(key));
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
		executeMapSingle(datas.keySet(), (jedis, poolKeys)->{
			List<String> resList = new ArrayList<>();
			poolKeys.forEach(key->{
				resList.add(jedis.setex(key, seconds, datas.get(key)));
			});
			return resList;
		});
	}
	
	/**
	 * 按keys批量获取
	 *	@ReturnType	String 
	 *	@Date	2018年1月30日	下午8:02:59
	 *  @Param  @param keys
	 *  @Param  @return
	 */
	public static List<String> getBatch(List<String> keys){
		return executeMapList(keys, (jedis, poolKeys)->jedis.mget(CollectionUtils.listToArray(poolKeys)));
	}
	
	/**
	 * 按keys批量移除
	 *	@ReturnType	long 
	 *	@Date	2018年1月30日	下午7:13:12
	 *  @Param  @return		删除的数量
	 */
	public static long removeBatch(List<String> keys){
		return executeMapSingle(keys, (jedis, poolKeys)->jedis.del(CollectionUtils.listToArray(poolKeys))).stream().mapToLong(x->x).sum();
	}

	/**
	 * 订阅频道
	 *	@ReturnType	void 
	 *	@Date	2018年2月3日	下午3:23:32
	 *  @Param  @param jedisPubSub
	 *  @Param  @param channel
	 */
	public static void subscribe(JedisPubSub jedisPubSub, String...channels){
		try (Jedis jedis = DEFAULT_POOL.getResource()) {
			jedis.subscribe(jedisPubSub, channels);
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
		try (Jedis jedis = DEFAULT_POOL.getResource()) {
			jedis.publish(channel, message);
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
		execute(setName, jedis->jedis.lpush(setName, values));
	}
	
	/**
	 * 向redis的set结构添加数据（添加在尾部）
	 *	@ReturnType	void 
	 *	@Date	2018年8月22日	下午3:42:37
	 *  @Param  @param setName			set的名称
	 *  @Param  @param values			要添加的数据
	 */
	public static void rpush(String setName, String...values){
		execute(setName, jedis->jedis.rpush(setName, values));
	}
	
	/**
	 * 从redis的set结构弹出数据（从头部获取并删除）
	 *	@ReturnType	String 
	 *	@Date	2018年8月22日	下午3:50:59
	 *  @Param  @param setName				set的名称
	 *  @Param  @return						获取到的value
	 */
	public static String lpop(String setName){
		return execute(setName, jedis->jedis.lpop(setName));
	}
	
	/**
	 * 从redis的set结构弹出数据（从尾部获取并删除）
	 *	@ReturnType	void 
	 *	@Date	2018年8月22日	下午3:42:37
	 *  @Param  @param setName				set的名称
	 *  @Param  @return						获取到的value
	 */
	public static String rpop(String setName){
		return execute(setName, jedis->jedis.rpop(setName));
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
		return execute(setName, jedis->jedis.blpop(timeout, setName));
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
		return execute(setName, jedis->jedis.brpop(timeout, setName));
	}
	
	/**
	 * 使用redis实现分布式锁
	 *	@ReturnType	void 
	 *	@Date	2018年9月21日	下午6:00:50
	 *  @Param  @param key					数据的key
	 *  @Param  @param timeout				超时时间（超时未获得锁会报异常）、锁存活最长时间。默认60s
	 */
	public static void lock(String key, Integer timeout){
		Assert.notNull(key, "key must not be nul");
		if (timeout == null || timeout <= 0) {
			timeout = 60;
		}
		// 初始时间
		Instant start = Instant.now();
		// 锁的key
		String lockKey = LOCK + key;
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
	}
	
	/**
	 * redis释放锁
	 *	@ReturnType	void 
	 *	@Date	2018年9月21日	下午6:00:41
	 *  @Param  @param key				数据的key
	 */
	public static void unlock(String key){
		Assert.notNull(key, "key must not be nul");
		// 锁的key
		String lockKey = LOCK + key;
		// 释放锁
		remove(lockKey);
		logger.info("redis...unlock...unlock success->" + lockKey);
	}
	
	/**
	 * 销毁所有连接池
	 *	@ReturnType	void 
	 *	@Date	2018年12月21日	下午2:02:35
	 *  @Param
	 */
	public static void destroyPool(){
		try {
			if (poolMap != null) {
				poolMap.values().forEach((x)->x.destroy());
			}
			if (DEFAULT_POOL != null) {
				DEFAULT_POOL.destroy();
			}
		} catch (Exception e) {
			logger.error("redis destroyPool error ===>", e);
		}
	}
	
	/********************************************************************
	 * private
	 ********************************************************************/
	
	/**
	 * 根据key获取JedisSentinelPool
	 *	@ReturnType	JedisSentinelPool 
	 *	@Date	2018年10月15日	下午3:30:20
	 *  @Param  @param key
	 *  @Param  @return
	 */
	private static JedisSentinelPool getPool(String key){
		if (StringUtils.isBlank(key)) {
			return DEFAULT_POOL;
		}
		// 按HashCode
		int index = Math.abs(key.hashCode() % mastersArr.length);
		String masterName = mastersArr[index];
		JedisSentinelPool pool = poolMap.get(masterName);
		if (pool == null) {
			return DEFAULT_POOL;
		}
		return pool;
	}
	
	/**
	 * 根据key获取pool，pool获取jedis
	 * 	执行函数(Jedis)->T
	 *	@ReturnType	T 
	 *	@Date	2018年12月25日	下午1:56:13
	 *  @Param  @param key
	 *  @Param  @param function
	 *  @Param  @return
	 */
    private static <T> T execute(String key, Function<Jedis, T> function) {
        JedisSentinelPool pool = getPool(key);
        try(Jedis jedis = pool.getResource()) {
            return function.apply(jedis);
        }
    }
    
    /**
     * 执行函数(Jedis)->Set<T>
     *	@ReturnType	List<T> 
     *	@Date	2018年12月25日	下午1:56:50
     *  @Param  @param function
     *  @Param  @return
     */
    private static <T> Set<T> executeBatchSet(Function<Jedis, Set<T>> function) {
    	Set<T> result = new HashSet<>();
        for (JedisSentinelPool pool : poolMap.values()) {
            try(Jedis jedis = pool.getResource()) {
                result.addAll(function.apply(jedis));
            }
        }
        return result;
    }
    
    /**
     * 传入Collection<String>，执行函数(Jedis, List<String>)->List<U>
     * 	返回几个函数的结果List<U>的合集List<U>
     *	@ReturnType	List<U> 
     *	@Date	2018年12月25日	下午3:49:32
     *  @Param  @param map
     *  @Param  @param function
     *  @Param  @return
     */
    private static <U> List<U> executeMapList(Collection<String> keys, BiFunction<Jedis, List<String>, List<U>> function) {
    	Map<JedisSentinelPool, List<String>> map = keys.stream().filter(x->x!=null).collect(Collectors.groupingBy(RedisCacheUtils::getPool));
    	List<U> totalValues = new ArrayList<>();
    	Set<JedisSentinelPool> pools = map.keySet();
    	for (JedisSentinelPool pool : pools) {
			try (Jedis jedis = pool.getResource()) {
				totalValues.addAll(function.apply(jedis, map.get(pool)));
			}
		}
    	return totalValues;
    }
    
    /**
     * 传入Collection<String>，执行函数(Jedis, List<String>)->U
     * 返回几个函数的结果U的集合List<U>
     *	@ReturnType	List<U> 
     *	@Date	2018年12月25日	下午3:49:32
     *  @Param  @param map
     *  @Param  @param function
     *  @Param  @return
     */
    private static <U> List<U> executeMapSingle(Collection<String> keys, BiFunction<Jedis, List<String>, U> function) {
		Map<JedisSentinelPool, List<String>> map = keys.stream().filter(x->x!=null).collect(Collectors.groupingBy(RedisCacheUtils::getPool));
    	List<U> totalValues = new ArrayList<>();
    	Set<JedisSentinelPool> pools = map.keySet();
    	for (JedisSentinelPool pool : pools) {
			try (Jedis jedis = pool.getResource()) {
				totalValues.add(function.apply(jedis, map.get(pool)));
			}
		}
    	return totalValues;
    }
    
}
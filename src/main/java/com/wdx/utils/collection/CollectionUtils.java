package com.wdx.utils.collection;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.http.util.Asserts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hutool.core.thread.NamedThreadFactory;

/**
 * 描述：处理集合类
 * @author 80002888 wdx
 * @date   2018年6月21日
 */
public class CollectionUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(CollectionUtils.class);
	
	/**
     * 按最大size拆分为子集合（实际为索引截取）并异步执行consumer
     * 如： 
     * 	集合大小：30 拆分大小：12 返回：3个集合，大小：12、12、6 
     * 	集合大小：30 拆分大小：4 返回：8个集合，大小：4、4、4、4、4、4、4、2 
     * 	集合大小：8 拆分大小：20
     * 
     * @ReturnType List<List<T>>
     * @Date 2018年10月17日 下午2:48:51
     * @Param @param list
     * @Param @param size 		每个集合最大size
     * @Param @param consumer 	每个子集合要执行的函数
     * @Param @return
     */
    public static <T, R> void splitListExecuteAsync(List<T> list, int size, final Consumer<List<T>> consumer) {
        Asserts.notNull(list, "list is null");
        Asserts.notNull(consumer, "consumer is null");
		long startTime = System.currentTimeMillis();
        int start = 0;
        int round = 1;
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
        		5, 
        		100, 
        		1, 
        		TimeUnit.SECONDS, 
        		new LinkedBlockingQueue<>(10), 
        		new NamedThreadFactory("splitListExecuteAsyncThread", false), 
        		new ThreadPoolExecutor.CallerRunsPolicy());
        do {
        	int end = 0;
            if (start + size > list.size() - 1) {
            	end = list.size();
            } else {
            	end = start + size;
            }
            final List<T> sonList = list.subList(start, end);
			try {
				logger.info(consumer + " round " + round);
				executor.execute(()->{
					consumer.accept(sonList);
				});
			} catch (Exception e) {
				logger.info("get error->" + consumer, e);
			}
            start = start + size;
            round ++;
        } while (start < list.size());
        
    	while (true) {
    		if (executor.getActiveCount() <= 0) {
    			executor.shutdown();
    			break;
    		}
		}
		long endTime = System.currentTimeMillis();
		logger.info("execute over, waste(s):" + (endTime-startTime)/1000);
    }
    
    /**
     * 按最大size拆分为子集合（实际为索引截取）并执行consumer
     * 如： 
     * 	集合大小：30 拆分大小：12 返回：3个集合，大小：12、12、6 
     * 	集合大小：30 拆分大小：4 返回：8个集合，大小：4、4、4、4、4、4、4、2 
     * 	集合大小：8 拆分大小：20
     * 
     * @ReturnType List<List<T>>
     * @Date 2018年10月17日 下午2:48:51
     * @Param @param list
     * @Param @param size 		每个集合最大size
     * @Param @param consumer 	每个子集合要执行的函数
     * @Param @return
     */
    public static <T, R> void splitListExecute(List<T> list, int size, Consumer<List<T>> consumer) {
        Asserts.notNull(list, "list is null");
        Asserts.notNull(consumer, "consumer is null");
		long startTime = System.currentTimeMillis();
        int start = 0;
        int round = 1;
        do {
        	int end = 0;
            if (start + size > list.size() - 1) {
            	end = list.size();
            } else {
            	end = start + size;
            }
            List<T> sonList = list.subList(start, end);
			try {
				logger.info(consumer + " round " + round);
				consumer.accept(sonList);
			} catch (Exception e) {
				logger.info("get error->" + consumer, e);
			}
            start = start + size;
            round ++;
        } while (start < list.size());
		long endTime = System.currentTimeMillis();
		logger.info("execute over, waste(s):" + (endTime-startTime)/1000);
    }
    
	/**
	 * 判断集合是否为空
	 *	@ReturnType	boolean 
	 *	@Date	2018年10月17日	下午2:53:49
	 *  @Param  @param coll
	 *  @Param  @return
	 */
	public static <T> boolean isEmpty(Collection<T> coll){
		if (coll == null || coll.size() == 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * 判断集合是否非空
	 *	@ReturnType	boolean 
	 *	@Date	2018年10月17日	下午2:53:49
	 *  @Param  @param coll
	 *  @Param  @return
	 */
	public static <T> boolean isNotEmpty(Collection<T> coll){
		if (coll != null && coll.size() != 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * 按最大size拆分集合，如：
	 * 			集合大小：30		拆分大小：12		返回：3个集合，大小：12、12、6
	 * 			集合大小：30		拆分大小：4		返回：8个集合，大小：4、4、4、4、4、4、4、2
	 * 			集合大小：8		拆分大小：20		返回：1个集合，大小：8
	 *	@ReturnType	List<List<T>> 
	 *	@Date	2018年10月17日	下午2:48:51
	 *  @Param  @param list
	 *  @Param  @param size		每个集合最大size
	 *  @Param  @return
	 */
	public static <T> List<List<T>> splitList(List<T> list, int size){
		if (list == null || list.size() == 0 || size == 0) {
			return null;
		}
		list = new ArrayList<T>(list);
		List<List<T>> resList = new ArrayList<>();
		int start = 0;
		do {
			if (start + size > list.size()-1) {
				resList.add(subListRight(list, start, list.size()));
			} else {
				resList.add(subListRight(list, start, start + size));
			}
			start = start + size;
		} while (start < list.size());
		return resList;
	}
	
	/**
	 * 平均拆分集合，如：
	 * 			集合大小：30		拆分大小：12		返回：4个集合，大小：7、7、8、8
	 * 			集合大小：30		拆分大小：4		返回：8个集合，大小：3、4、4、4、3、4、4、4
	 * 			集合大小：8		拆分大小：20		返回：1个集合，大小：8
	 *	@ReturnType	List<List<T>> 
	 *	@Date	2018年8月6日	上午9:45:28
	 *  @Param  @param list				要被拆分的集合
	 *  @Param  @param threshold		拆分的大小
	 *  @Param  @return
	 */
	public static <T> List<List<T>> splitList(List<T> list, Integer threshold){
		if (list == null || list.size() == 0 || threshold == null || threshold == 0) {
			return null;
		}
		list = new ArrayList<T>(list);
		List<List<T>> resList = new ArrayList<>();
		int size = list.size();
		if (size <= threshold) {
			resList.add(list);
		} else {
			int middle = size/2;
			List<T> leftList = list.subList(0, middle);
			List<T> rightList = list.subList(middle, size);
			List<List<T>> leftRes = splitList(leftList, threshold);
			List<List<T>> rightRes = splitList(rightList, threshold);
			resList.addAll(leftRes);
			resList.addAll(rightRes);
		}
		return resList;
	} 
	
	/**
	 * 截取集合，返回新的集合
	 *	@ReturnType	List<T> 
	 *	@Date	2018年4月20日	下午6:23:15
	 *  @Param  @param list
	 *  @Param  @param fromIndex
	 *  @Param  @param toIndex
	 *  @Param  @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> subListRight(List<T> list, Integer fromIndex, Integer toIndex){
		if (CollectionUtils.isEmpty(list)) {
			return null;
		}
		if (toIndex > list.size()) {
			toIndex = list.size();
		}
		Class<? extends List<T>> clazz = (Class<? extends List<T>>) list.getClass();
		try {
			Constructor<?>[] constructors = clazz.getConstructors();
			if (constructors != null && constructors.length != 0) {
				for (Constructor<?> constructor : constructors) {
					constructor.setAccessible(true);
				}
			}
			List<T> newList = clazz.newInstance();
			for (int i = fromIndex; i < toIndex; i++) {
				newList.add(list.get(i));
			}
			if (CollectionUtils.isEmpty(newList)) {
				return null;
			}
			return newList;
		} catch (Exception e) {
			logger.error("-----------ListUtil...subListRight get error->"+e.getMessage(), e);
		}
		return null;
	}
	
	/**
	 * 数组转List集合，会去除数组中的null元素
	 *	@ReturnType	List<T> 
	 *	@Date	2018年9月8日	上午10:47:29
	 *  @Param  @param array
	 *  @Param  @return
	 */
	public static <T> List<T> arrayToList(T[] array){
		if (array == null || array.length == 0) {
			return null;
		}
		List<T> arrayList = Arrays.asList(array);
		if (arrayList == null || arrayList.size() == 0) {
			return null;
		}
		List<T> res = new ArrayList<>(arrayList);
		return res.stream().filter((x)->x!=null).collect(Collectors.toList());
	}
	
	/**
	 * 数组转Set集合，会去除数组中的null元素
	 *	@ReturnType	null<T> 
	 *	@Date	2018年9月8日	上午10:49:58
	 *  @Param  @param array
	 *  @Param  @return
	 */
	public static <T> Set<T> arrayToSet(T[] array){
		if (array == null || array.length == 0) {
			return null;
		}
		List<T> list = arrayToList(array);
		if (list == null || list.size() == 0) {
			return null;
		}
		return new HashSet<>(list);
	}
	
	/**
	 * List集合转为数组，会去除list中的null元素
	 *	@ReturnType	T[] 
	 *	@Date	2018年1月30日	下午7:53:29
	 *  @Param  @param list
	 *  @Param  @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] listToArray(List<T> list){
		if (CollectionUtils.isEmpty(list)) {
			return null;
		}
		list = list.stream().filter((x)->x!=null).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(list)) {
			return null;
		}
		Class<? extends Object> clazz = list.get(0).getClass();
		T[] array = (T[]) Array.newInstance(clazz, list.size());
		return list.toArray(array);
	}

	/**
	 * Set集合转为数组
	 *	@ReturnType	T[] 
	 *	@Date	2018年2月2日	下午2:06:56
	 *  @Param  @param set
	 *  @Param  @return
	 */
	public static <T> T[] setToArray(Set<T> set){
		if (CollectionUtils.isEmpty(set)) {
			return null;
		}
		List<T> list = new ArrayList<>(set);
		return listToArray(list);
	}
	
}

package com.wdx.utils.collection;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 描述：处理list
 * @author 80002888
 * @date   2018年6月21日
 */
public class ListUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(ListUtil.class);
	
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
		Class<? extends List<T>> clazz = (Class<? extends List<T>>) list.getClass();
		try {
			Constructor<?>[] constructors = clazz.getConstructors();
			for (Constructor<?> constructor : constructors) {
				constructor.setAccessible(true);
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

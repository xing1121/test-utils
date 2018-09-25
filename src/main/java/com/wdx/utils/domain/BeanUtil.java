package com.wdx.utils.domain;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * 描述：复制A对象到B对象（A对象的null属性不赋值）
 * @author 80002888
 * @date   2018年6月15日
 */
public class BeanUtil {

	public static void main(String[] args) {
		User u1 = new User();
		u1.setId(1L);
		u1.setName("张三");
		u1.setUsername("aaa123");
		u1.setPassword("123456");
		System.out.println(u1);
		
		User u2 = new User();
		u2.setPassword("123456789");
		System.out.println(u2);
		
		System.out.println("------------------------------");
		
		copyPropertiesIgnoreNull(u2, u1);
		System.out.println(u1);
		System.out.println(u2);
	}
	
	/**
	 * 获取空属性的名称
	 *	@ReturnType	String[] 
	 *	@Date	2018年6月15日	下午5:48:26
	 *  @Param  @param source
	 *  @Param  @return
	 */
	public static String[] getNullPropertyNames (Object source) {
		if (source == null) {
			return null;
		}
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();
        Set<String> emptyNames = new HashSet<String>();
        for(PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
            	emptyNames.add(pd.getName());
            }
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }
	
	/**
	 * src的属性设置到target（src的null属性不赋值）
	 *	@ReturnType	void 
	 *	@Date	2018年6月15日	下午5:48:30
	 *  @Param  @param src
	 *  @Param  @param target
	 */
    public static void copyPropertiesIgnoreNull(Object src, Object target){
        BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
    }
	
}

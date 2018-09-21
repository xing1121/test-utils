package com.wdx.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

public class SpringFactory implements BeanFactoryAware {
	
	private static BeanFactory beanFactory;

	@Override
	public void setBeanFactory(BeanFactory factory) throws BeansException {
		SpringFactory.beanFactory = factory;
	}

	public static BeanFactory getBeanFactory() {
		return beanFactory;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getBean(String beanName) {
		if (beanFactory == null) {
			return null;
		}
		Object object = beanFactory.getBean(beanName);
		if (object == null) {
			return null;
		}
		return (T) beanFactory.getBean(beanName);
	}

}

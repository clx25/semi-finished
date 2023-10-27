package com.semifinished.util;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.function.Supplier;

/**
 * 用来注册bean的工具类
 */
public class SpringBeanUtils {

    public static <T> void registerBean(DefaultListableBeanFactory defaultListableBeanFactory, Class<T> clazz, Supplier<T> supplier, String name) {
        AbstractBeanDefinition transactionTemplateBeanDefinition =
                BeanDefinitionBuilder.genericBeanDefinition(clazz, supplier).getBeanDefinition();
        defaultListableBeanFactory.registerBeanDefinition(name, transactionTemplateBeanDefinition);
    }

    public static <T> T getBean(DefaultListableBeanFactory defaultListableBeanFactory, Class<T> clazz) {
        return defaultListableBeanFactory.getBean(clazz);
    }


}

package com.semifinished.util.bean;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * 用来注册bean的工具类
 */
@Component
@AllArgsConstructor
public class SpringBeanUtils {
    private final DefaultListableBeanFactory defaultListableBeanFactory;

    /**
     * 注册bean
     *
     * @param clazz    bean类型
     * @param supplier 返回值作为bean注册到spring容器中
     * @param name     bean名称
     */
    public <T> void registerBean(Class<T> clazz, Supplier<T> supplier, String name) {
        AbstractBeanDefinition transactionTemplateBeanDefinition =
                BeanDefinitionBuilder.genericBeanDefinition(clazz, supplier).getBeanDefinition();
        defaultListableBeanFactory.registerBeanDefinition(name, transactionTemplateBeanDefinition);
    }

    /**
     * 根据类型获取bean
     *
     * @param clazz bean类型
     * @return bean
     */
    public <T> T getBean(Class<T> clazz) {
        return defaultListableBeanFactory.getBean(clazz);
    }


}

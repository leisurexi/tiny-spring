package com.leisurexi.tiny.spring.beans.factory.config;

import com.leisurexi.tiny.spring.beans.factory.DefaultListableBeanFactory;

/**
 * BeanFactory 后置处理器
 *
 * @author: leisurexi
 * @date: 2020-06-02 23:14
 * @since 0.0.5
 */
@FunctionalInterface
public interface BeanFactoryPostProcessor {

    /**
     * BeanFactory 的后置处理
     *
     * @param beanFactory bean 工厂
     */
    void postProcessBeanFactory(DefaultListableBeanFactory beanFactory);

}

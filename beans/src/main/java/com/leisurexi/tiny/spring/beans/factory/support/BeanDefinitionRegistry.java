package com.leisurexi.tiny.spring.beans.factory.support;

import com.leisurexi.tiny.spring.beans.exception.BeansException;

/**
 * BeanDefinition 注册中心接口
 *
 * @author: leisurexi
 * @date: 2020-04-05 2:40 下午
 * @since 0.0.1
 */
public interface BeanDefinitionRegistry {

    /**
     * 注册 bean definition
     *
     * @param beanName       bean 名称
     * @param beanDefinition bean 定义元信息
     */
    void registryBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeansException;


    /**
     * 返回当前注册中心的 bean definition 数量
     */
    int getBeanDefinitionCount();

}

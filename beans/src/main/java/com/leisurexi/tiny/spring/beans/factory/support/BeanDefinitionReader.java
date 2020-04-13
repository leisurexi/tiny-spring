package com.leisurexi.tiny.spring.beans.factory.support;

import com.leisurexi.tiny.spring.beans.exception.BeansException;

/**
 * 从配置中读取 BeanDefinition
 *
 * @author: leisurexi
 * @date: 2020-04-04 5:30 下午
 * @since 0.0.1
 */
public interface BeanDefinitionReader {

    /**
     * 从文件中读取 BeanDefinition 并注册
     *
     * @param location 文件路径
     * @return 读取到的 Bean 数量
     * @throws BeansException
     */
    int loadBeanDefinitions(String location) throws BeansException;

}

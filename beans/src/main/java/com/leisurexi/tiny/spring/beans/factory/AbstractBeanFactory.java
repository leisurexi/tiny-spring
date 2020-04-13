package com.leisurexi.tiny.spring.beans.factory;

import com.leisurexi.tiny.spring.beans.exception.BeansException;
import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinition;

/**
 * Bean 工厂基本实现
 *
 * @author: leisurexi
 * @date: 2020-04-04 2:39 下午
 * @since 0.0.1
 */
public abstract class AbstractBeanFactory implements BeanFactory {

    @Override
    public Object getBean(String name) {
        return createBean(name);
    }

    /**
     * 创建 Bean
     *
     * @param name bean 的名称
     * @return bean 实例
     */
    protected abstract Object createBean(String name) throws BeansException;

}

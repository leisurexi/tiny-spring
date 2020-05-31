package com.leisurexi.tiny.spring.context;

import com.leisurexi.tiny.spring.beans.factory.BeanFactory;
import com.leisurexi.tiny.spring.beans.factory.DefaultListableBeanFactory;

/**
 * 应用上下文，{@link BeanFactory} 的扩展
 * 提供以下功能：
 * 1.注解
 * 2.事件
 *
 * @author: leisurexi
 * @date: 2020-05-31 19:14
 * @since 0.0.4
 */
public interface ApplicationContext extends BeanFactory {

    /**
     * 获取底层 IoC 容器，BeanFactory
     */
    DefaultListableBeanFactory getBeanFactory();

}

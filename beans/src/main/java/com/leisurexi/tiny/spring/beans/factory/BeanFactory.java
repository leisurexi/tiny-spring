package com.leisurexi.tiny.spring.beans.factory;

/**
 * 抽象 Bean 工厂
 *
 * @author: leisurexi
 * @date: 2020-04-04 14:20
 * @since 0.0.1
 */
public interface BeanFactory {

    /**
     * 按照名称获取 bean
     *
     * @param beanName bean 名称
     * @return bean 的实例
     */
    Object getBean(String beanName);

    /**
     * 按照类型获取 bean
     *
     * @param requiredType bean 的类型
     * @return bean 的实例
     * @since 0.0.3
     */
    <T> T getBean(Class<T> requiredType);

    /**
     * 按照名称和类型获取 bean
     *
     * @param beanName bean 的名称
     * @param requiredType bean 的类型
     * @return bean 的类型
     * @since 0.0.3
     */
    <T> T getBean(String beanName, Class<T> requiredType);

}

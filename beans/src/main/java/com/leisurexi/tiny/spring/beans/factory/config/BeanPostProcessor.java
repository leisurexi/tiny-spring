package com.leisurexi.tiny.spring.beans.factory.config;

/**
 * bean 的后置处理器
 *
 * @author: leisurexi
 * @date: 2020-05-01 11:12 下午
 * @since 0.0.3
 */
public interface BeanPostProcessor {

    /**
     * bean 初始化前回调，此时 bean 已经实例化并且属性已经赋值
     *
     * @param bean
     * @param beanName
     * @return
     */
    default Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    /**
     * bean 初始化后回调
     *
     * @param bean
     * @param beanName
     * @return
     */
    default Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }

}

package com.leisurexi.tiny.spring.beans.factory.config;

import com.leisurexi.tiny.spring.beans.PropertyValues;
import com.leisurexi.tiny.spring.beans.factory.AbstractAutowireCapableBeanFactory;
import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinition;

/**
 * bean 的实例化接口扩展
 *
 * @author: leisurexi
 * @date: 2020-05-01 11:17 下午
 * @since 0.0.3
 */
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {

    /**
     * bean 实例化前回调，返回非 {@code null} 会跳过后面流程直接返回
     *
     * @param beanClass bean 的类型
     * @param beanName  bean 的名称
     * @return 实例，可能是代理对象
     * @see AbstractAutowireCapableBeanFactory#doCreateBean(String, BeanDefinition)
     */
    default Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) {
        return null;
    }

    /**
     * bean 实例化后回调，这个阶段还没有属性赋值，返回 {@code false} 会跳过后面流程
     *
     * @param bean     bean 的实例
     * @param beanName bean 的名称
     * @return {@code false} 跳过后面流程，{@code true} 按默认流程继续执行
     */
    default boolean postProcessAfterInstantiation(Object bean, String beanName) {
        return true;
    }

    /**
     * bean 实例化后属性赋值前阶段，PropertyValues 是已经解析好的属性值，
     * 返回 {@code null} 继续使用现有属性，否则会替换 PropertyValues
     *
     * @param pvs 解析好的属性值
     * @param bean           bean 的实例
     * @param beanName       bean 的名称
     * @return {@code null} 继续使用现有的属性值，否则用返回的替换原来的 PropertyValues
     */
    default PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) {
        return null;
    }

}

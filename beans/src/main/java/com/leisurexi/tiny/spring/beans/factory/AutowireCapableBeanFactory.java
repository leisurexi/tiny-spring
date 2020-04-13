package com.leisurexi.tiny.spring.beans.factory;

import cn.hutool.core.util.ReflectUtil;
import com.google.common.reflect.Reflection;
import com.leisurexi.tiny.spring.beans.PropertyValues;
import com.leisurexi.tiny.spring.beans.exception.BeansException;
import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinition;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

/**
 * 可自动装配的 BeanFactory
 *
 * @author: leisurexi
 * @date: 2020-04-04 2:42 下午
 * @since 0.0.1
 */
@Slf4j
public abstract class AutowireCapableBeanFactory extends AbstractBeanFactory {

    @Override
    protected Object createBean(String name) throws BeansException {
        BeanDefinition beanDefinition = getBeanDefinition(name);
        if (beanDefinition == null) {
            throw new IllegalStateException("no such bean definition for " + name);
        }
        Object bean = createBeanInstance(beanDefinition);
        if (beanDefinition.getPropertyValues() != null) {
            // 给 bean 的属性赋值
            applyPropertyValues(bean, beanDefinition.getPropertyValues());
        }
        return bean;
    }

    /**
     * 根据 bean 的名称 获取 bean 的定义元信息
     *
     * @param beanName bean 的名称
     * @return bean 的定义元信息
     * @throws BeansException
     */
    protected abstract BeanDefinition getBeanDefinition(String beanName) throws BeansException;

    /**
     * 利用反射创建实例
     *
     * @param beanDefinition bean的定义元数据
     * @return bean 的实例
     * @throws BeansException
     */
    protected Object createBeanInstance(BeanDefinition beanDefinition) throws BeansException {
        try {
            // 实例化 bean
            return beanDefinition.getBeanClass().newInstance();
        } catch (InstantiationException e) {
            throw new BeansException(e);
        } catch (IllegalAccessException e) {
            throw new BeansException(e);
        }
    }

    /**
     * 给 bean 的属性赋值
     *
     * @param bean           目标 bean
     * @param propertyValues 多个属性值
     * @throws BeansException
     */
    protected void applyPropertyValues(Object bean, PropertyValues propertyValues) throws BeansException {
        propertyValues.forEach(propertyValue -> ReflectUtil.setFieldValue(bean, propertyValue.getName(), propertyValue.getValue()));
    }

}

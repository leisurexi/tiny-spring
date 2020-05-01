package com.leisurexi.tiny.spring.beans.factory;

import cn.hutool.core.util.ReflectUtil;
import com.leisurexi.tiny.spring.beans.PropertyValues;
import com.leisurexi.tiny.spring.beans.exception.BeansException;
import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinition;
import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinitionValueResolver;
import com.leisurexi.tiny.spring.beans.factory.support.ConstructorResolver;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;


/**
 * 可自动装配的 BeanFactory
 *
 * @author: leisurexi
 * @date: 2020-04-04 2:42 下午
 * @since 0.0.1
 */
@Slf4j
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory {

    @Override
    protected Object createBean(String beanName, BeanDefinition beanDefinition) throws BeansException {
        return doCreateBean(beanName, beanDefinition);
    }

    /**
     * 为指定的 bean 创建实例
     *
     * @param beanName       bean 的名称
     * @param beanDefinition bean 的定义元数据
     * @return bean 的实例
     * @since 0.0.3
     */
    protected Object doCreateBean(String beanName, BeanDefinition beanDefinition) {
        Object bean = createBeanInstance(beanName, beanDefinition);
        if (beanDefinition.getPropertyValues() != null) {
            // 给 bean 的属性赋值
            applyPropertyValues(bean, beanDefinition.getPropertyValues());
        }
        return bean;
    }

    /**
     * 利用反射创建实例
     *
     * @param beanName       bean 的名称
     * @param beanDefinition bean 的定义元数据
     * @return bean 的实例
     * @throws BeansException
     * @since 0.0.3
     */
    protected Object createBeanInstance(String beanName, BeanDefinition beanDefinition) throws BeansException {
        if (beanDefinition.hasConstructorArgumentValues()) {
            return autowireConstructor(beanName, beanDefinition);
        }
        return instantiateBean(beanDefinition);
    }

    /**
     * 使用默认构造函数实例化 bean
     *
     * @param beanDefinition bean 定义元信息
     * @return bean 实例
     * @since 0.0.3
     */
    private Object instantiateBean(BeanDefinition beanDefinition) {
        Class<?> clazz = beanDefinition.getBeanClass();
        if (clazz.isInterface()) {
            throw new BeansException("Specified class is an interface");
        }
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            throw new BeansException("No default constructor found");
        }
    }

    /**
     * 找个匹配的构造函数进行实例化
     *
     * @param beanName       bean 的名称
     * @param beanDefinition bean 的定义元信息
     * @return bean 的实例
     * @since 0.0.3
     */
    private Object autowireConstructor(String beanName, BeanDefinition beanDefinition) {
        return new ConstructorResolver(this).autowireConstructor(beanName, beanDefinition);
    }

    /**
     * 给 bean 的属性赋值
     *
     * @param bean           目标 bean
     * @param propertyValues 多个属性值
     * @throws BeansException
     */
    protected void applyPropertyValues(Object bean, PropertyValues propertyValues) throws BeansException {
        BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this);
        propertyValues.forEach(propertyValue -> ReflectUtil.setFieldValue(bean, propertyValue.getName(), valueResolver.resolveValueIfNecessary(propertyValue.getName(), propertyValue.getValue())));
    }

}

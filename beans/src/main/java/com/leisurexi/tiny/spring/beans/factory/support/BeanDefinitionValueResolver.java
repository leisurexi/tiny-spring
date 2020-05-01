package com.leisurexi.tiny.spring.beans.factory.support;

import com.leisurexi.tiny.spring.beans.factory.AbstractAutowireCapableBeanFactory;
import com.leisurexi.tiny.spring.beans.factory.config.RuntimeBeanReference;

/**
 * bean 工厂实现的帮助类，解析 bean 定义对象中的值，
 * 转换为应用与目标 bean 实例的实际值
 *
 * @author: leisurexi
 * @date: 2020-04-30 0:26
 * @since 0.0.3
 */
public class BeanDefinitionValueResolver {

    /**
     * bean 工厂
     */
    private final AbstractAutowireCapableBeanFactory beanFactory;

    public BeanDefinitionValueResolver(AbstractAutowireCapableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * 解析属性实际值，如果是引用别的 bean，会去 beanFactory 查找
     *
     * @param name  属性名称
     * @param value 属性值
     * @return 属性实际值
     */
    public Object resolveValueIfNecessary(String name, Object value) {
        if (value instanceof RuntimeBeanReference) {
            RuntimeBeanReference reference = RuntimeBeanReference.class.cast(value);
            return resolveReference(name, reference);
        } else {
            return value;
        }
    }

    /**
     * 返回引用的 bean 实例
     */
    private Object resolveReference(String name, RuntimeBeanReference runtimeBeanReference) {
        String beanName = runtimeBeanReference.getBeanName();
        return this.beanFactory.getBean(beanName);
    }

}

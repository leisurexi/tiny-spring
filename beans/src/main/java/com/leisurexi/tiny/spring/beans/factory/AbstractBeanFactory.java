package com.leisurexi.tiny.spring.beans.factory;

import cn.hutool.core.convert.Convert;
import com.leisurexi.tiny.spring.beans.exception.BeansException;
import com.leisurexi.tiny.spring.beans.factory.config.BeanPostProcessor;
import com.leisurexi.tiny.spring.beans.factory.config.Scope;
import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinition;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.leisurexi.tiny.spring.beans.factory.support.BeanDefinition.SCOPE_PROTOTYPE;
import static com.leisurexi.tiny.spring.beans.factory.support.BeanDefinition.SCOPE_SINGLETON;

/**
 * Bean 工厂基本实现
 *
 * @author: leisurexi
 * @date: 2020-04-04 2:39 下午
 * @since 0.0.1
 */
@Slf4j
public abstract class AbstractBeanFactory implements BeanFactory {

    /**
     * 单例 bean 的缓存，key 为 bean 的名称，value 是 bean 的实例
     */
    protected final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

    /**
     * 单例 bean 的 ObjectFactory 的缓存，key 为 bean 的名称，value 是 ObjectFactory 的实例
     * 为解决循环依赖使用
     *
     * @since 0.0.3
     */
    protected final Map<String, ObjectFactory<?>> singletonFactories = new ConcurrentHashMap<>(256);

    /**
     * 自定义作用域保存容器
     */
    protected final Map<String, Scope> scopes = new LinkedHashMap<>(8);

    /**
     * 保存注册的 bean 的扩展接口
     */
    protected final List<BeanPostProcessor> beanPostProcessors = new CopyOnWriteArrayList<>();

    @Override
    public Object getBean(String beanName) {
        return doGetBean(beanName, null);
    }

    @Override
    public <T> T getBean(String beanName, Class<T> requiredType) {
        return doGetBean(beanName, requiredType);
    }

    /**
     * 真正去获取 bean 的方法
     *
     * @param beanName bean的名称
     * @return bean 实例
     * @since 0.0.2
     */
    private <T> T doGetBean(String beanName, Class<T> requiredType) {
        BeanDefinition beanDefinition = getBeanDefinition(beanName);
        if (beanDefinition == null) {
            throw new BeansException("no such bean definition for " + beanName);
        }
        Object bean = getSingleton(beanName);
        // 判断单例缓存中是否存在需要获取的 bean
        if (bean != null) {
            log.debug("hit singleton cache, beanName: [{}]", beanName);
        } else {
            if (beanDefinition.isSingleton()) {
                // 单例作用域，创建完实例缓存起来
                bean = createBean(beanName, beanDefinition);
                // 添加单例 bean 到缓存中
                addSingleton(beanName, bean);
            } else if (beanDefinition.isPrototype()) {
                // 原型作用域，每次新创建一个实例
                bean = createBean(beanName, beanDefinition);
            } else {
                // 自定义作用域
                String scopeName = beanDefinition.getScope();
                Scope scope = scopes.get(scopeName);
                if (scope == null) {
                    throw new IllegalStateException("No Scope registered for scope name '" + scopeName + "'");
                }
                bean = scope.get(scopeName, () -> createBean(beanName, beanDefinition));
            }
        }

        // 需要 bean 的类型不为空，进行类型转换
        if (requiredType != null) {
            // 通过第三方工具类进行类型转换，失败会抛出异常
            T convertBean = Convert.convert(requiredType, bean);
            return convertBean;
        }
        return (T) bean;
    }

    /**
     * 注册自定义作用域
     *
     * @param scope 自定义作用域 {@link Scope}
     * @since 0.0.2
     */
    public void registerScope(Scope scope) {
        if (SCOPE_SINGLETON.equals(scope.scopeName()) || SCOPE_PROTOTYPE.equals(scope.scopeName())) {
            throw new IllegalArgumentException("Cannot replace existing scopes 'singleton' and 'prototype'");
        }
        Scope previous = scopes.put(scope.scopeName(), scope);
        if (previous != null && previous != scope) {
            log.debug("Replacing scope '{}' from [{}] to [{}]", scope.scopeName(), previous, scope);
        } else {
            log.debug("Registering scope '{}' with implementation [{}]", scope.scopeName(), scope);
        }
    }

    /**
     * 添加 bean 的扩展接口
     *
     * @param beanPostProcessor
     */
    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        this.beanPostProcessors.remove(beanPostProcessor);
        this.beanPostProcessors.add(beanPostProcessor);
    }

    /**
     * 返回所有已经注册的 bean 的扩展接口
     */
    public List<BeanPostProcessor> getBeanPostProcessors() {
        return this.beanPostProcessors;
    }

    /**
     * 获取单例 bean 的缓存
     *
     * @param beanName
     * @return
     */
    protected Object getSingleton(String beanName) {
        Object singletonObject = singletonObjects.get(beanName);
        if (singletonObject == null) {
            ObjectFactory<?> singletonFactory = singletonFactories.get(beanName);
            if (singletonFactory != null) {
                singletonObject = singletonFactory.getObject();
            }
        }
        return singletonObject;
    }


    /**
     * 将给定的单例对象添加到该工厂的单例缓存中
     *
     * @param beanName         bean 的名称
     * @param singletonFactory 单例对象
     */
    protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
        synchronized (this.singletonObjects) {
            // 如果已经初始化完成的单例 bean 缓存中不存在，则添加 singletonFactory
            // 到单例工厂缓存中
            if (!this.singletonObjects.containsKey(beanName)) {
                this.singletonFactories.put(beanName, singletonFactory);
            }
        }
    }

    /**
     * 将给定的单例对象添加到缓存中
     *
     * @param beanName        bean 的名称
     * @param singletonObject 单例对象
     */
    protected void addSingleton(String beanName, Object singletonObject) {
        synchronized (this.singletonObjects) {
            // 将 bean 实例缓存起来
            this.singletonObjects.put(beanName, singletonObject);
            // 移除 bean 的工厂
            this.singletonFactories.remove(beanName);
        }
    }

    /**
     * 创建 Bean
     *
     * @param name bean 的名称
     * @return bean 实例
     * @since 0.0.2
     */
    protected abstract Object createBean(String name, BeanDefinition beanDefinition) throws BeansException;

    /**
     * 根据 bean 的名称 获取 bean 的定义元信息
     *
     * @param beanName bean 的名称
     * @return bean 的定义元信息
     * @throws BeansException
     */
    protected abstract BeanDefinition getBeanDefinition(String beanName) throws BeansException;

}

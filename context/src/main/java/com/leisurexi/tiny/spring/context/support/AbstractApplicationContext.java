package com.leisurexi.tiny.spring.context.support;

import com.leisurexi.tiny.spring.beans.exception.BeansException;
import com.leisurexi.tiny.spring.beans.factory.DefaultListableBeanFactory;
import com.leisurexi.tiny.spring.beans.factory.config.BeanFactoryPostProcessor;
import com.leisurexi.tiny.spring.beans.factory.config.BeanPostProcessor;
import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinition;
import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinitionRegistry;
import com.leisurexi.tiny.spring.context.ApplicationContext;
import com.leisurexi.tiny.spring.context.annotation.AnnotationConfigUtils;

import java.util.List;

/**
 * @author: leisurexi
 * @date: 2020-05-31 19:20
 * @since 0.0.4
 */
public abstract class AbstractApplicationContext implements ApplicationContext, BeanDefinitionRegistry {

    /**
     * 当前上下文的 bean factory
     */
    private DefaultListableBeanFactory beanFactory;

    /**
     * 上下文刷新方法，也可以理解为上下文启动的方法
     */
    public void refresh() {
        // 刷新 beanFactory
        refreshBeanFactory();
        // 注册注解配置处理器
        AnnotationConfigUtils.registerAnnotationConfigProcessors(this);
        // 调用 beanFactory 的后置处理器
        invokeBeanFactoryPostProcessors(beanFactory);
        // 注册 bean 的后置处理器
        registerBeanPostProcessors(beanFactory);
        // 完成 beanFactory 的初始化
        finishBeanFactoryInitialization(beanFactory);
    }

    /**
     * 调用 beanFactory 的后置处理器
     *
     * @param beanFactory bean 工厂
     */
    protected void invokeBeanFactoryPostProcessors(DefaultListableBeanFactory beanFactory) {
        List<String> beanNames = beanFactory.beanNamesForType(BeanFactoryPostProcessor.class);
        for (String beanName : beanNames) {
            BeanFactoryPostProcessor beanFactoryPostProcessor = (BeanFactoryPostProcessor) beanFactory.getBean(beanName);
            beanFactoryPostProcessor.postProcessBeanFactory(beanFactory);
        }
    }

    /**
     * 完成 beanFactory 的初始化
     *
     * @param beanFactory bean 工厂
     */
    protected void finishBeanFactoryInitialization(DefaultListableBeanFactory beanFactory) {
        // 提前初始化单例 bean
        beanFactory.preInstantiateSingletons();
    }

    /**
     * 注册 bean 的后置处理器
     *
     * @param beanFactory bean 工厂
     */
    protected void registerBeanPostProcessors(DefaultListableBeanFactory beanFactory) {
        List<String> beanNames = beanFactory.beanNamesForType(BeanPostProcessor.class);
        for (String beanName : beanNames) {
            beanFactory.addBeanPostProcessor(getBean(beanName, BeanPostProcessor.class));
        }
    }

    /**
     * 刷新 BeanFactory，这里比较粗暴，直接 new 一个 DefaultListableBeanFactory
     * 赋值给 beanFactory
     */
    protected void refreshBeanFactory() {
        this.beanFactory = new DefaultListableBeanFactory();
        loadBeanDefinitions(this.beanFactory);
    }

    /**
     * 加载 bean 的定义元信息，模板方法由子类实现
     *
     * @param beanFactory bean 的工厂
     */
    protected abstract void loadBeanDefinitions(DefaultListableBeanFactory beanFactory);

    //---------------------------------------------------------------------
    // 实现 BeanFactory 接口，方法实现全部委托给内部的 beanFactory
    //---------------------------------------------------------------------
    @Override
    public Object getBean(String beanName) {
        return this.beanFactory.getBean(beanName);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) {
        return this.beanFactory.getBean(requiredType);
    }

    @Override
    public <T> T getBean(String beanName, Class<T> requiredType) {
        return this.beanFactory.getBean(beanName, requiredType);
    }

    @Override
    public List<String> getBeanDefinitionNames() {
        return this.beanFactory.getBeanDefinitionNames();
    }

    //---------------------------------------------------------------------
    // 实现 BeanDefinitionRegistry 接口，方法实现全部委托给内部的 beanFactory
    //---------------------------------------------------------------------
    @Override
    public void registryBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeansException {
        this.beanFactory.registryBeanDefinition(beanName, beanDefinition);
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return this.beanFactory.containsBeanDefinition(beanName);
    }

    @Override
    public int getBeanDefinitionCount() {
        return this.beanFactory.getBeanDefinitionCount();
    }

    @Override
    public DefaultListableBeanFactory getBeanFactory() {
        return this.beanFactory;
    }
}

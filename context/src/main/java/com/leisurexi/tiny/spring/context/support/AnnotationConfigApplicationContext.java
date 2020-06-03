package com.leisurexi.tiny.spring.context.support;

import com.leisurexi.tiny.spring.beans.factory.DefaultListableBeanFactory;
import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinition;
import com.leisurexi.tiny.spring.context.annotation.AnnotationConfigUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: leisurexi
 * @date: 2020-05-31 22:47
 * @since 0.0.4
 */
public class AnnotationConfigApplicationContext extends AbstractApplicationContext {

    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    /**
     * 扫描注定包路径下的类，找到标注 @Component 的类并注册
     *
     * @param basePackage 包路径
     */
    public void scan(String... basePackage) {
        doScan(basePackage);
    }

    /**
     * 注册指定的类
     *
     * @param componentClasses 指定组件类
     */
    public void register(Class<?>... componentClasses) {
        this.beanDefinitionMap = AnnotationConfigUtils.registerClass(componentClasses);
    }

    /**
     * 找到符合条件的组件，也就是标注了 @Component 注解的类型，
     * 并封装成 BeanDefinition 注册进 beanFactory
     *
     * @param basePackages 包路径
     * @return 符合条件的组件定义元信息集合
     */
    private void doScan(String... basePackages) {
        this.beanDefinitionMap = AnnotationConfigUtils.scanComponent(basePackages);
    }

    @Override
    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) {
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            beanFactory.registryBeanDefinition(entry.getKey(), entry.getValue());
        }
    }

}

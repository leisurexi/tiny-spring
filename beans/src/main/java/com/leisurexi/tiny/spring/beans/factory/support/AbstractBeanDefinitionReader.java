package com.leisurexi.tiny.spring.beans.factory.support;

import com.leisurexi.tiny.spring.beans.io.ResourceLoader;

/**
 * @author: leisurexi
 * @date: 2020-04-04 7:10 下午
 * @since 0.0.1
 */
public abstract class AbstractBeanDefinitionReader implements BeanDefinitionReader {

    protected final BeanDefinitionRegistry registry;
    private ResourceLoader resourceLoader;

    public AbstractBeanDefinitionReader(BeanDefinitionRegistry registry) {
        this.registry = registry;
        this.resourceLoader = new ResourceLoader();
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    public BeanDefinitionRegistry getBeanRegistry() {
        return registry;
    }
}

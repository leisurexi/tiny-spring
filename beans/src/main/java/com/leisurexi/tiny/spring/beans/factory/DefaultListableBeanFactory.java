package com.leisurexi.tiny.spring.beans.factory;

import com.leisurexi.tiny.spring.beans.exception.BeansException;
import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinition;
import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinitionRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: leisurexi
 * @date: 2020-04-05 2:44 下午
 * @since 0.0.1
 */
public class DefaultListableBeanFactory extends AutowireCapableBeanFactory implements BeanDefinitionRegistry {

    /**
     * 存储 bean definition 的 Map，key 是 bean 的名称，value 就是 BeanDefinition
     */
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);

    /**
     * 存储注册顺序的 bean definition names
     */
    private final List<String> beanDefinitionNames = new ArrayList<>();

    @Override
    public void registryBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeansException {
        BeanDefinition existingDefinition = beanDefinitionMap.get(beanName);
        // BeanDefinition 已经被注册过，抛出异常
        if (existingDefinition != null) {
            throw new BeansException(String.format("[%s] already existing definition. existing definition: [%s]", beanName, existingDefinition));
        }
        beanDefinitionMap.put(beanName, beanDefinition);
        beanDefinitionNames.add(beanName);
    }

    @Override
    public int getBeanDefinitionCount() {
        return this.beanDefinitionMap.size();
    }

    @Override
    protected BeanDefinition getBeanDefinition(String beanName) throws BeansException {
        return beanDefinitionMap.get(beanName);
    }
}

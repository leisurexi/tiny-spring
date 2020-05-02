package com.leisurexi.tiny.spring.beans.factory;

import com.leisurexi.tiny.spring.beans.exception.BeansException;
import com.leisurexi.tiny.spring.beans.factory.config.DependencyDescriptor;
import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinition;
import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinitionRegistry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: leisurexi
 * @date: 2020-04-05 2:44 下午
 * @since 0.0.1
 */
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory implements BeanDefinitionRegistry {

    /**
     * 存储 bean definition 的 Map，key 是 bean 的名称，value 就是 BeanDefinition
     */
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);

    /**
     * 存储注册顺序的 bean definition names
     */
    private final List<String> beanDefinitionNames = new ArrayList<>();

    /**
     * 存储所有注册过的 Class 和 beanName 的对象关系
     */
    private final Map<Class<?>, List<String>> allBeanNamesByType = new ConcurrentHashMap<>(64);

    @Override
    public void registryBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeansException {
        BeanDefinition existingDefinition = beanDefinitionMap.get(beanName);
        // BeanDefinition 已经被注册过，抛出异常
        if (existingDefinition != null) {
            throw new BeansException(String.format("[%s] already existing definition. existing definition: [%s]", beanName, existingDefinition));
        }
        // 缓存 beanDefinition，如果是原型作用域可以重复使用
        beanDefinitionMap.put(beanName, beanDefinition);
        beanDefinitionNames.add(beanName);
        List<String> beanNames = allBeanNamesByType.get(beanDefinition.getBeanClass());
        if (beanNames == null) {
            beanNames = new ArrayList<>();
        }
        beanNames.add(beanName);
    }

    @Override
    public int getBeanDefinitionCount() {
        return this.beanDefinitionMap.size();
    }

    @Override
    protected BeanDefinition getBeanDefinition(String beanName) throws BeansException {
        return beanDefinitionMap.get(beanName);
    }

    @Override
    public Object resolveDependency(DependencyDescriptor descriptor, String requestingBeanName) {
        return doResolveDependency(descriptor, requestingBeanName);
    }

    public Object doResolveDependency(DependencyDescriptor descriptor, String requestingBeanName) {
        Class<?> type = descriptor.getDependencyType();
        Map<String, Object> matchingBeans = findAutowireCandidates(requestingBeanName, type, descriptor);
        if (matchingBeans.size() > 1) {
            // 符合条件的 bean 有多个
            Object result = matchingBeans.get(descriptor.getParameterName());
            if (result == null) {
                // 未找到名字为参数名的 bean
                throw new BeansException("No such bean " + descriptor.getParameterName());
            }
            return result;
        } else {
            // 符合条件的 bean 只有一个
            return matchingBeans.entrySet().iterator().next().getValue();
        }
    }

    /**
     * 根据指定的类型找到 bean 的实例
     *
     * @param beanName    bean 的名称
     * @param requireType 需要查找的 bean 的类型
     * @param descriptor  依赖描述符
     * @return 符合条件的 bean，beanName->bean 为 key->value 对
     */
    private Map<String, Object> findAutowireCandidates(String beanName, Class<?> requireType, DependencyDescriptor descriptor) {
        List<String> candidateNames = beanNamesForType(requireType);
        Map<String, Object> result = new LinkedHashMap<>(candidateNames.size());
        for (String candidateName : candidateNames) {
            if (beanName.equals(candidateName)) {
                throw new IllegalStateException("Can not autowire same type");
            }
            result.put(candidateName, getBean(candidateName));
        }
        return result;
    }

    /**
     * 根据类型找到所有符合条件的 bean 名称
     */
    private List<String> beanNamesForType(Class<?> requireType) {
        return this.allBeanNamesByType.get(requireType);
    }


}
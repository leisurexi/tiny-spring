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
        allBeanNamesByType.put(beanDefinition.getBeanClass(), beanNames);
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return this.beanDefinitionMap.containsKey(beanName);
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

    @Override
    public <T> T getBean(Class<T> requiredType) {
        // 找到该类型的所有 bean
        List<String> candidateNames = beanNamesForType(requiredType);
        // 默认返回第一个 bean
        return candidateNames.isEmpty() ? null : getBean(candidateNames.get(0), requiredType);
    }

    public Object doResolveDependency(DependencyDescriptor descriptor, String requestingBeanName) {
        Class<?> type = descriptor.getDependencyType();
        Map<String, Object> matchingBeans = findAutowireCandidates(requestingBeanName, type);
        if (matchingBeans.isEmpty()) {
            return null;
        }
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
     * @return 符合条件的 bean，beanName->bean 为 key->value 对
     */
    private Map<String, Object> findAutowireCandidates(String beanName, Class<?> requireType) {
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
     * 找到所有相同类型或者其子类型的 bean 名称
     *
     * @since 0.0.4
     */
    public List<String> beanNamesForType(Class<?> requiredType) {
        List<String> beanNames = new ArrayList<>();
        for (Map.Entry<Class<?>, List<String>> entry : allBeanNamesByType.entrySet()) {
            if (requiredType.isAssignableFrom(entry.getKey())) {
                beanNames.addAll(entry.getValue());
            }
        }
        return beanNames;
    }

    /**
     * 提前初始化单例 bean
     */
    public void preInstantiateSingletons() {
        List<String> beanNames = new ArrayList<>(beanDefinitionNames);
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = getBeanDefinition(beanName);
            if (beanDefinition.isSingleton()) {
                getBean(beanName);
            }
        }
    }

}

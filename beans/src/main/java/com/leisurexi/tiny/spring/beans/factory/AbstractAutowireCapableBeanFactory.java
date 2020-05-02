package com.leisurexi.tiny.spring.beans.factory;

import cn.hutool.core.util.ReflectUtil;
import com.leisurexi.tiny.spring.beans.PropertyValues;
import com.leisurexi.tiny.spring.beans.exception.BeansException;
import com.leisurexi.tiny.spring.beans.factory.config.BeanPostProcessor;
import com.leisurexi.tiny.spring.beans.factory.config.DependencyDescriptor;
import com.leisurexi.tiny.spring.beans.factory.config.InstantiationAwareBeanPostProcessor;
import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinition;
import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinitionValueResolver;
import com.leisurexi.tiny.spring.beans.factory.support.ConstructorResolver;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.util.Set;


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
        Object bean = resolveBeforeInstantiation(beanName, beanDefinition);
        if (bean != null) {
            return bean;
        }
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
     * 创建 bean 的实例
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

    /**
     * 实例化前应用后置处理器，以解决是否存在指定 bean 的快捷创建方式
     *
     * @param beanName       bean 的名称
     * @param beanDefinition bean 的定义元信息
     * @return 快捷方式创建的 bean 实例，或者为空
     */
    protected Object resolveBeforeInstantiation(String beanName, BeanDefinition beanDefinition) {
        // 执行 bean 的实例化前回调
        Object bean = applyBeanPostProcessorBeforeInstantiation(beanDefinition.getBeanClass(), beanName);
        // 如果返回的不为空，那么直接执行 bean 的初始化后回调，因为只能在这里执行了
        if (bean != null) {
            bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
        }
        return bean;
    }

    /**
     * 调用 bean 实例化之前的回调方法
     *
     * @param beanClass bean 的类型
     * @param beanName  bean 的名称
     * @return
     */
    protected Object applyBeanPostProcessorBeforeInstantiation(Class<?> beanClass, String beanName) {
        for (BeanPostProcessor beanPostProcessor : getBeanPostProcessors()) {
            if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
                InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) beanPostProcessor;
                Object result = ibp.postProcessBeforeInstantiation(beanClass, beanName);
                log.debug("[{}] 执行实例化前回调，返回: [{}]", beanName, result);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * bean 的初始化后回调
     *
     * @param existingBean bean 的实例
     * @param beanName     bean 的名称
     * @return
     */
    protected Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName) {
        Object result = existingBean;
        for (BeanPostProcessor beanPostProcessor : getBeanPostProcessors()) {
            Object current = beanPostProcessor.postProcessAfterInitialization(result, beanName);
            log.debug("[{}] 执行初始化后回调，返回: [{}]", beanName, current);
            if (current == null) {
                return result;
            }
            result = current;
        }
        return result;
    }

    /**
     * 解决指定 bean 的依赖关系
     *
     * @param descriptor         依赖描述符
     * @param requestingBeanName 需要解决依赖的 bean 名称
     * @return 符合条件的 bean，或者 {@code null} 如果没找到
     * @since 0.0.3
     */
    public abstract Object resolveDependency(DependencyDescriptor descriptor, String requestingBeanName);

}

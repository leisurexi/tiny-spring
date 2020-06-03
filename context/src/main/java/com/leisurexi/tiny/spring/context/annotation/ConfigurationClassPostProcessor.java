package com.leisurexi.tiny.spring.context.annotation;

import com.google.common.base.Strings;
import com.leisurexi.tiny.spring.beans.factory.DefaultListableBeanFactory;
import com.leisurexi.tiny.spring.beans.factory.config.BeanFactoryPostProcessor;
import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinition;
import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinitionRegistry;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: leisurexi
 * @date: 2020-06-02 23:39
 * @since 0.0.5
 */
public class ConfigurationClassPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(DefaultListableBeanFactory beanFactory) {
        List<String> beanNames = new ArrayList<>(beanFactory.getBeanDefinitionNames());
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            Class beanClass = beanDefinition.getBeanClass();
            if (beanClass.isAnnotationPresent(Configuration.class)) {
                if (beanClass.isAnnotationPresent(ComponentScan.class)) {
                    ComponentScan componentScan = (ComponentScan) beanClass.getAnnotation(ComponentScan.class);
                    String[] basePackages = componentScan.basePackages();
                    if (basePackages.length > 0) {
                        Map<String, BeanDefinition> beanDefinitionMap = AnnotationConfigUtils.scanComponent(basePackages);
                        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
                            beanFactory.registryBeanDefinition(entry.getKey(), entry.getValue());
                        }
                    }
                }
                retrieveBeanMethodMetadata(beanName, beanFactory, beanDefinition.getBeanClass());
            }
        }
    }

    /**
     * 将标记 {@link Bean} 注解的方法构建成 {@link BeanDefinition} 并注册进 bean 的注册中心
     *
     * @param configBeanName 标注了 {@link Configuration} 注解的 bean 的名称
     * @param registry       bean 的注册中心
     * @param clazz          标注了 {@link Configuration} 注解的 bean 的类型
     */
    private void retrieveBeanMethodMetadata(String configBeanName, BeanDefinitionRegistry registry, Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Bean.class)) {
                BeanDefinition beanDefinition = new BeanDefinition();
                Bean bean = method.getAnnotation(Bean.class);
                String beanName = bean.name();
                String initMethod = bean.initMethod();
                Scope scope = method.getAnnotation(Scope.class);
                if (scope != null && !Strings.isNullOrEmpty(scope.value())) {
                    beanDefinition.setScope(scope.value());
                }
                if (Strings.isNullOrEmpty(beanName)) {
                    beanName = method.getName();
                }
                if (!Strings.isNullOrEmpty(initMethod)) {
                    beanDefinition.setInitMethodName(initMethod);
                }
                beanDefinition.setBeanClass(method.getReturnType());
                beanDefinition.setFactoryMethod(method);
                beanDefinition.setFactoryBeanName(configBeanName);
                registry.registryBeanDefinition(beanName, beanDefinition);
            }
        }
    }

}

package com.leisurexi.tiny.spring.context.support;

import cn.hutool.core.util.ClassUtil;
import com.google.common.base.Strings;
import com.leisurexi.tiny.spring.beans.factory.DefaultListableBeanFactory;
import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinition;
import com.leisurexi.tiny.spring.context.annotation.Component;
import com.leisurexi.tiny.spring.context.annotation.Scope;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.leisurexi.tiny.spring.beans.factory.support.BeanDefinition.SCOPE_SINGLETON;

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
    public void scan(String basePackage) {
        doScan(basePackage);
    }

    /**
     * 找到符合条件的组件，也就是标注了 @Component 注解的类型，
     * 并封装成 BeanDefinition 注册进 beanFactory
     *
     * @param basePackage 包路径
     * @return 符合条件的组件定义元信息集合
     */
    private void doScan(String basePackage) {
        DefaultListableBeanFactory beanFactory = getBeanFactory();
        // 获取路径下，所有标注了 @Component 注解的类
        Set<Class<?>> classes = ClassUtil.scanPackageByAnnotation(basePackage, Component.class);
        for (Class<?> clazz : classes) {
            BeanDefinition beanDefinition = new BeanDefinition();
            beanDefinition.setBeanClass(clazz);
            beanDefinition.setBeanClassName(clazz.getName());
            String beanName = clazz.getAnnotation(Component.class).value();
            // 如果没有显示指定 beanName，那么就把类型首字母转成小写当做 beanName
            if (Strings.isNullOrEmpty(beanName)) {
                beanName = initialsConvertLowerCase(clazz.getSimpleName());
            }
            Scope scope = clazz.getAnnotation(Scope.class);
            String scopeName;
            // 如果没有指定作用域，默认是单例
            if (scope != null && !Strings.isNullOrEmpty(scope.value())) {
                scopeName = scope.value();
            } else {
                scopeName = SCOPE_SINGLETON;
            }
            beanDefinition.setScope(scopeName);
            if(beanDefinitionMap.containsKey(beanName)){
                throw new IllegalStateException(beanName + " already has bean definition, please check bean name");
            }
            beanDefinitionMap.put(beanName, beanDefinition);
        }
    }

    @Override
    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) {
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            beanFactory.registryBeanDefinition(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 首字母转换为小写
     */
    private String initialsConvertLowerCase(String str) {
        if (Strings.isNullOrEmpty(str)) {
            return null;
        }
        char c = str.charAt(0);
        if (c >= 'A' && c <= 'Z') {
            c += 32;
        }
        char[] chars = str.toCharArray();
        chars[0] = c;
        return new String(chars);
    }

}

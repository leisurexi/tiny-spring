package com.leisurexi.tiny.spring.context.annotation;

import cn.hutool.core.util.ClassUtil;
import com.google.common.base.Strings;
import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinition;
import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinitionRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.leisurexi.tiny.spring.beans.factory.support.BeanDefinition.SCOPE_SINGLETON;

/**
 * @author: leisurexi
 * @date: 2020-06-01 22:28
 * @since JDK 1.8
 */
public class AnnotationConfigUtils {

    public static final String AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME =
            "org.leisurexi.tiny.spring.context.annotation.internalAutowiredAnnotationProcessor";

    /**
     * 注册注解配置处理器
     *
     * @see AutowiredAnnotationBeanPostProcessor
     */
    public static void registerAnnotationConfigProcessors(BeanDefinitionRegistry registry) {
        if (!registry.containsBeanDefinition(AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME)) {
            registry.registryBeanDefinition(AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME, new BeanDefinition(AutowiredAnnotationBeanPostProcessor.class));
        }
    }

    /**
     * 扫描组件并注册进 bean 注册中心
     *
     * @param basePackage 包路径
     */
    public static Map<String, BeanDefinition> scanComponentRegisterBeanRegistry(String basePackage) {
        Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();
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
            if (beanDefinitionMap.containsKey(beanName)) {
                throw new IllegalStateException(beanName + " already has bean definition, please check bean name");
            }
            beanDefinitionMap.put(beanName, beanDefinition);
        }
        return beanDefinitionMap;
    }

    /**
     * 首字母转换为小写
     */
    private static String initialsConvertLowerCase(String str) {
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

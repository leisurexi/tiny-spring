package com.leisurexi.tiny.spring.context.annotation;

import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinition;
import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinitionRegistry;

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

}

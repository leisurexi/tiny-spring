package com.leisurexi.tiny.spring.context.config;

import com.google.common.base.Strings;
import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinition;
import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinitionRegistry;
import com.leisurexi.tiny.spring.beans.factory.support.xml.NamespaceHandler;
import com.leisurexi.tiny.spring.context.annotation.AnnotationConfigUtils;
import org.w3c.dom.Element;

import java.util.Map;

/**
 * XML context:component-scan 标签的处理
 *
 * @author: leisurexi
 * @date: 2020-06-02 22:09
 * @since 0.0.4
 */
public class ContextNamespaceHandler implements NamespaceHandler {

    @Override
    public String namespace() {
        return "context:component-scan";
    }

    @Override
    public void parse(BeanDefinitionRegistry registry, Element element) {
        String basePackage = element.getAttribute("base-package");
        if (Strings.isNullOrEmpty(basePackage)) {
            throw new IllegalArgumentException("base-package attribute must not be null");
        }

        Map<String, BeanDefinition> beanDefinitionMap = AnnotationConfigUtils.scanComponent(basePackage);
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            registry.registryBeanDefinition(entry.getKey(), entry.getValue());
        }
    }

}

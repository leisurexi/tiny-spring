package com.leisurexi.tiny.spring.beans.factory.support.xml;

import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinitionRegistry;
import org.w3c.dom.Element;

/**
 * XML 自定义命名空间的处理
 * 使用方法：
 * 1.指定命名空间的名称，如 context:。
 * 2.重写 parse(Element) 方法，实现对该命名空间节点的自定义解析。
 *
 * @author: leisurexi
 * @date: 2020-06-02 22:01
 * @since 0.0.4
 */
public interface NamespaceHandler {

    /**
     * 节点的命名空间
     */
    String namespace();

    /**
     * 节点处理
     *
     * @param registry bean 注册中心
     * @param element  节点元素
     */
    void parse(BeanDefinitionRegistry registry, Element element);

}

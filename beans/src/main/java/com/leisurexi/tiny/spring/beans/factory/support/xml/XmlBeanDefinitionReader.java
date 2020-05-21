package com.leisurexi.tiny.spring.beans.factory.support.xml;

import com.google.common.base.Strings;
import com.leisurexi.tiny.spring.beans.PropertyValue;
import com.leisurexi.tiny.spring.beans.PropertyValues;
import com.leisurexi.tiny.spring.beans.exception.BeansException;
import com.leisurexi.tiny.spring.beans.factory.config.ConstructorArgumentValues;
import com.leisurexi.tiny.spring.beans.factory.config.RuntimeBeanReference;
import com.leisurexi.tiny.spring.beans.factory.support.AbstractBeanDefinitionReader;
import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinition;
import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinitionRegistry;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;

import static com.leisurexi.tiny.spring.beans.factory.support.BeanDefinition.*;

/**
 * 从 XML 文件读取 Bean 配置实现
 *
 * @author: leisurexi
 * @date: 2020-04-04 7:11 下午
 * @since 0.0.1
 */
@Slf4j
public class XmlBeanDefinitionReader extends AbstractBeanDefinitionReader {

    public XmlBeanDefinitionReader(BeanDefinitionRegistry registry) {
        super(registry);
    }

    @Override
    public int loadBeanDefinitions(String location) throws BeansException {
        InputStream inputStream;
        try {
            inputStream = getResourceLoader().getResource(location).getInputStream();
        } catch (IOException e) {
            throw new BeansException(e);
        }
        return doLoadBeanDefinitions(inputStream);
    }

    /**
     * 将输入流构建成 Document 然后解析并注册 bean definition
     *
     * @param inputStream 输入流
     * @return 此次加载 bean 的数量
     */
    protected int doLoadBeanDefinitions(InputStream inputStream) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = factory.newDocumentBuilder();
            Document document = documentBuilder.parse(inputStream);
            // 解析并注册 bean definition
            return registerBeanDefinitions(document);
        } catch (Exception e) {
            throw new BeansException(e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new BeansException(e);
            }
        }
    }

    /**
     * 注册 BeanDefinition
     *
     * @param document
     */
    protected int registerBeanDefinitions(Document document) {
        Element element = document.getDocumentElement();
        int countBefore = getBeanRegistry().getBeanDefinitionCount();
        parseBeanDefinitions(element);
        return getBeanRegistry().getBeanDefinitionCount() - countBefore;
    }

    /**
     * 解析 XML 节点中的内容，并转换为 BeanDefinition
     *
     * @param root 根节点
     */
    protected void parseBeanDefinitions(Element root) {
        NodeList childNodes = root.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node instanceof Element) {
                Element element = Element.class.cast(node);
                processBeanDefinition(element);
            }
        }
    }

    /**
     * 解析 XML 节点 id 和 class 节点值，并为 beanDefinition 设置
     *
     * @param element 节点
     * @since 0.0.2
     */
    protected void processBeanDefinition(Element element) {
        String id = element.getAttribute("id");
        if (Strings.isNullOrEmpty(id)) {
            throw new IllegalStateException("id attribute must bo not bull");
        }
        String className = element.getAttribute("class");
        if (Strings.isNullOrEmpty(className)) {
            throw new IllegalStateException("className attribute must bo not bull");
        }
        String scope = element.getAttribute("scope");
        if (Strings.isNullOrEmpty(scope)) {
            scope = "singleton";
        }
        String autowire = element.getAttribute("autowire");
        if (Strings.isNullOrEmpty(autowire)) {
            autowire = "no";
        }
        int autowireMode = AUTOWIRE_NO;
        if (autowire != null) {
            if (!"no".equals(autowire) && !"byName".equals(autowire) && !"byType".equals(autowire) && !"constructor".equals(autowire)) {
                throw new IllegalArgumentException("Attribute autowire only support 'no' or 'byName' or 'byType' or 'constructor'");
            }
            switch (autowire) {
                case "byName":
                    autowireMode = AUTOWIRE_BY_NAME;
                    break;
                case "byType":
                    autowireMode = AUTOWIRE_BY_TYPE;
                    break;
                case "constructor":
                    autowireMode = AUTOWIRE_CONSTRUCTOR;
                    break;
            }
        }

        // bean 初始化方法名称
        String initMethodName = element.getAttribute("init-method");

        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setBeanClassName(className);
        beanDefinition.setScope(scope);
        beanDefinition.setAutowireMode(autowireMode);
        beanDefinition.setInitMethodName(initMethodName);
        try {
            beanDefinition.setBeanClass(Class.forName(className));
        } catch (ClassNotFoundException e) {
            throw new BeansException(e);
        }
        processProperty(element, beanDefinition);
        processConstructorArgs(element, beanDefinition);
        getBeanRegistry().registryBeanDefinition(id, beanDefinition);
        log.debug("加载 Bean: [{}]，具体信息: [{}]", id, beanDefinition);
    }

    /**
     * 解析 XML 节点 constructor-arg 节点值，并为 beanDefinition 设置
     *
     * @param element        节点
     * @param beanDefinition bean 定义元信息
     */
    private void processConstructorArgs(Element element, BeanDefinition beanDefinition) {
        NodeList constructorArgsNode = element.getElementsByTagName("constructor-arg");
        ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
        for (int i = 0; i < constructorArgsNode.getLength(); i++) {
            Node node = constructorArgsNode.item(i);
            if (node instanceof Element) {
                Element constructorArgsEle = Element.class.cast(node);
                Integer index = Integer.valueOf(constructorArgsEle.getAttribute("index"));
                String value = constructorArgsEle.getAttribute("value");
                String refName = constructorArgsEle.getAttribute("ref");
                if (!Strings.isNullOrEmpty(value) && !Strings.isNullOrEmpty(refName)) {
                    throw new IllegalArgumentException("Only allowed to contain either 'ref' attribute or 'value' attribute");
                }
                if (!Strings.isNullOrEmpty(value)) {
                    constructorArgumentValues.addIndexArgumentValue(index, value);
                } else {
                    constructorArgumentValues.addIndexArgumentValue(index, new RuntimeBeanReference(refName));
                }
            }
        }
        beanDefinition.setConstructorArgumentValues(constructorArgumentValues);
    }

    /**
     * 解析 XML 节点 property 节点值，并为 beanDefinition 设置
     *
     * @param element        节点
     * @param beanDefinition bean 定义元信息
     * @since 0.0.3
     */
    private void processProperty(Element element, BeanDefinition beanDefinition) {
        NodeList propertyNode = element.getElementsByTagName("property");
        PropertyValues propertyValues = new PropertyValues();
        for (int i = 0; i < propertyNode.getLength(); i++) {
            Node node = propertyNode.item(i);
            if (node instanceof Element) {
                Element propertyEle = Element.class.cast(node);
                String name = propertyEle.getAttribute("name");
                String value = propertyEle.getAttribute("value");
                String refName = propertyEle.getAttribute("ref");
                PropertyValue propertyValue;
                if (!Strings.isNullOrEmpty(value) && !Strings.isNullOrEmpty(refName)) {
                    throw new IllegalArgumentException("Only allowed to contain either 'ref' attribute or 'value' attribute");
                }
                if (!Strings.isNullOrEmpty(value)) {
                    propertyValue = new PropertyValue(name, value);
                } else {
                    propertyValue = new PropertyValue(name, new RuntimeBeanReference(refName));
                }
                propertyValues.addPropertyValues(propertyValue);
            }
        }
        beanDefinition.setPropertyValues(propertyValues);
    }

}

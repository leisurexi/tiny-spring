package com.leisurexi.tiny.spring.beans.factory.support.xml;

import com.google.common.base.Strings;
import com.leisurexi.tiny.spring.beans.PropertyValue;
import com.leisurexi.tiny.spring.beans.PropertyValues;
import com.leisurexi.tiny.spring.beans.exception.BeansException;
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
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setBeanClassName(className);
        beanDefinition.setPropertyValues(new PropertyValues());
        try {
            beanDefinition.setBeanClass(Class.forName(className));
        } catch (ClassNotFoundException e) {
            throw new BeansException(e);
        }
        processProperty(element, beanDefinition);
        getBeanRegistry().registryBeanDefinition(id, beanDefinition);
        log.info("加载 Bean: {}，具体信息: {}", id, beanDefinition);
    }

    /**
     * 解析 XML 节点 property 节点值，并为 beanDefinition 设置
     *
     * @param element        节点
     * @param beanDefinition Bean 定义元信息
     */
    private void processProperty(Element element, BeanDefinition beanDefinition) {
        NodeList propertyNode = element.getElementsByTagName("property");
        for (int i = 0; i < propertyNode.getLength(); i++) {
            Node node = propertyNode.item(i);
            if (node instanceof Element) {
                Element propertyEle = Element.class.cast(node);
                String name = propertyEle.getAttribute("name");
                String value = propertyEle.getAttribute("value");
                beanDefinition.getPropertyValues().addPropertyValues(new PropertyValue(name, value));
            }
        }
    }

}

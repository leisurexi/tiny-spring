package com.leisurexi.tiny.spring.beans.xml;

import com.leisurexi.tiny.spring.beans.domain.User;
import com.leisurexi.tiny.spring.beans.factory.DefaultListableBeanFactory;
import com.leisurexi.tiny.spring.beans.factory.support.xml.XmlBeanDefinitionReader;
import com.leisurexi.tiny.spring.beans.io.ResourceLoader;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author: leisurexi
 * @date: 2020-04-04 7:41 下午
 * @since 0.0.1
 */
@Slf4j
public class XmlBeanDefinitionReaderTest {

    @Test
    public void test() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
        int count = beanDefinitionReader.loadBeanDefinitions("META-INF/xml-beans.xml");
        log.info("加载 Bean 的数量: {}", count);
        User user = (User) beanFactory.getBean("user");
        log.info(user.toString());
    }

}

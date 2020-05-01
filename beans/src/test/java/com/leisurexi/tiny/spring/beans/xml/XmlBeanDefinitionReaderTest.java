package com.leisurexi.tiny.spring.beans.xml;

import com.leisurexi.tiny.spring.beans.domain.User;
import com.leisurexi.tiny.spring.beans.factory.DefaultListableBeanFactoryAbstract;
import com.leisurexi.tiny.spring.beans.factory.support.xml.XmlBeanDefinitionReader;
import com.leisurexi.tiny.spring.beans.scope.ThreadLocalScope;
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
    public void getBeanTest() {
        DefaultListableBeanFactoryAbstract beanFactory = new DefaultListableBeanFactoryAbstract();
        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
        int count = beanDefinitionReader.loadBeanDefinitions("META-INF/xml-beans.xml");
        log.info("加载 Bean 的数量: {}", count);
        User user = (User) beanFactory.getBean("user");
        log.info(user.toString());
    }

    @Test
    public void singletonTest() {
        DefaultListableBeanFactoryAbstract beanFactory = new DefaultListableBeanFactoryAbstract();
        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
        beanDefinitionReader.loadBeanDefinitions("META-INF/xml-beans.xml");
        User user = (User) beanFactory.getBean("user");
        log.info(user.toString());
        User user1 = (User) beanFactory.getBean("user");
        log.info(user1.toString());
    }

    @Test
    public void scopeTest() throws InterruptedException {
        DefaultListableBeanFactoryAbstract beanFactory = new DefaultListableBeanFactoryAbstract();
        beanFactory.registerScope(new ThreadLocalScope());
        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
        beanDefinitionReader.loadBeanDefinitions("META-INF/xml-beans.xml");
        for (int i = 0; i < 3; i++) {
            Thread thread = new Thread(() -> {
                User user = (User) beanFactory.getBean("thread-local-user");
                System.err.printf("[Thread id :%d] user = %s%n", Thread.currentThread().getId(), user.getClass().getName() + "@" + Integer.toHexString(user.hashCode()));
                User user1 = (User) beanFactory.getBean("thread-local-user");
                System.err.printf("[Thread id :%d] user1 = %s%n", Thread.currentThread().getId(), user1.getClass().getName() + "@" + Integer.toHexString(user1.hashCode()));
            });
            thread.start();
            thread.join();
        }
    }

}

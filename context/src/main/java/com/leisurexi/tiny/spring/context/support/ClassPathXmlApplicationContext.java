package com.leisurexi.tiny.spring.context.support;

import com.leisurexi.tiny.spring.beans.factory.DefaultListableBeanFactory;
import com.leisurexi.tiny.spring.beans.factory.support.xml.XmlBeanDefinitionReader;

/**
 * XML 应用程序上下文
 *
 * @author: leisurexi
 * @date: 2020-05-31 21:13
 * @since 0.0.4
 */
public class ClassPathXmlApplicationContext extends AbstractApplicationContext {

    /**
     * 配置文件地址
     */
    protected String[] configLocations;

    /**
     * @param location 单个文件地址
     */
    public ClassPathXmlApplicationContext(String location) {
        this(new String[]{location});
    }

    /**
     * @param locations 多个文件地址
     */
    public ClassPathXmlApplicationContext(String... locations) {
        if (locations == null) {
            throw new IllegalArgumentException("locations must not be null");
        }
        configLocations = locations;
        // 调用父类方法，刷新上下文
        refresh();
    }

    @Override
    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) {
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
        for (int i = 0; i < configLocations.length; i++) {
            String configLocation = configLocations[i];
            // 加载配置文件中 bean 的定义
            reader.loadBeanDefinitions(configLocation);
        }
    }
}

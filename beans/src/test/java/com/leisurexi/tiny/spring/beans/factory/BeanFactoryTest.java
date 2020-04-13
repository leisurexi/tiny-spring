package com.leisurexi.tiny.spring.beans.factory;

import com.leisurexi.tiny.spring.beans.PropertyValue;
import com.leisurexi.tiny.spring.beans.PropertyValues;
import com.leisurexi.tiny.spring.beans.domain.User;
import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinition;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author: leisurexi
 * @date: 2020-04-04 14:26
 * @since 0.0.1
 */
@Slf4j
public class BeanFactoryTest {

    @Test
    public void test() {
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setBeanClass(User.class);
        PropertyValues propertyValues = new PropertyValues();
        propertyValues.addPropertyValues(new PropertyValue("id", 1L));
        propertyValues.addPropertyValues(new PropertyValue("name", "leisurexi"));
        beanDefinition.setPropertyValues(propertyValues);
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registryBeanDefinition("user", beanDefinition);
        User user = (User) beanFactory.getBean("user");
        log.info(user.toString());
    }

}

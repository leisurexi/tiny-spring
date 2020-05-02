package com.leisurexi.tiny.spring.beans.beanprocessor;

import com.leisurexi.tiny.spring.beans.domain.User;
import com.leisurexi.tiny.spring.beans.factory.config.InstantiationAwareBeanPostProcessor;

/**
 * @author: leisurexi
 * @date: 2020-05-01 11:54 下午
 * @since JDK 1.8
 */
public class MyInstantiationAwareBeanProcessor implements InstantiationAwareBeanPostProcessor {

    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) {
        if ("user".equals(beanName)) {
            User user = new User();
            return user;
        }
        return null;
    }
}

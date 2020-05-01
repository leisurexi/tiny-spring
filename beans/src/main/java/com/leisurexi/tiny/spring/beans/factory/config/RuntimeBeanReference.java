package com.leisurexi.tiny.spring.beans.factory.config;

import lombok.Getter;

/**
 * 对工厂中另一个 bean 的引用，在运行时解析
 *
 * @author: leisurexi
 * @date: 2020-04-30 0:11
 * @since 0.0.3
 */
@Getter
public class RuntimeBeanReference {

    /** 引用的 bean 的名称 */
    private final String beanName;

    public RuntimeBeanReference(String beanName) {
        this.beanName = beanName;
    }

}


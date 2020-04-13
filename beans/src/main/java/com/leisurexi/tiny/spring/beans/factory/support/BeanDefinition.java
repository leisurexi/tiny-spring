package com.leisurexi.tiny.spring.beans.factory.support;

import com.leisurexi.tiny.spring.beans.PropertyValues;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * bean 定义的元信息
 *
 * @author: leisurexi
 * @date: 2020-04-04 14:21
 * @since 0.0.1
 */
@Getter
@Setter
@ToString
@Slf4j
public class BeanDefinition {

    /**
     * bean 的 Class对象
     */
    private Class beanClass;

    /**
     * bean 的全类名
     */
    private String beanClassName;

    /**
     * bean 的属性
     */
    private PropertyValues propertyValues;

}

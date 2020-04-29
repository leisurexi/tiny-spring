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

    public static final String SCOPE_SINGLETON = "singleton";

    public static final String SCOPE_PROTOTYPE = "prototype";

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

    /**
     * bean 作用域
     * @since 0.0.2
     */
    private String scope;

    /**
     * bean 的作用域是否是单例
     * @see #SCOPE_SINGLETON
     * @since 0.0.2
     */
    public boolean isSingleton() {
        return SCOPE_SINGLETON.equals(this.scope);
    }

    /**
     * bean 的作用域是否原型
     * @see #SCOPE_PROTOTYPE
     * @since 0.0.2
     */
    public boolean isPrototype() {
        return SCOPE_PROTOTYPE.equals(this.scope);
    }

}

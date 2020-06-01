package com.leisurexi.tiny.spring.beans.factory.support;

import com.leisurexi.tiny.spring.beans.PropertyValues;
import com.leisurexi.tiny.spring.beans.factory.config.ConstructorArgumentValues;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


/**
 * bean 定义的元信息
 *
 * @author: leisurexi
 * @date: 2020-04-04 14:21
 * @since 0.0.1
 */
@Data
@Slf4j
public class BeanDefinition {

    public static final String SCOPE_SINGLETON = "singleton";

    public static final String SCOPE_PROTOTYPE = "prototype";

    /**
     * 不自动装配
     *
     * @since 0.0.3
     */
    public static final int AUTOWIRE_NO = 0;

    /**
     * 按名称注入
     *
     * @since 0.0.3
     */
    public static final int AUTOWIRE_BY_NAME = 1;

    /**
     * 按类型注入
     *
     * @since 0.0.3
     */
    public static final int AUTOWIRE_BY_TYPE = 2;

    /**
     * 构造器注入
     *
     * @since 0.0.3
     */
    public static final int AUTOWIRE_CONSTRUCTOR = 3;

    /**
     * 依赖注入类型，默认不依赖注入
     *
     * @since 0.0.3
     */
    private int autowireMode = AUTOWIRE_NO;

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
    private PropertyValues propertyValues = new PropertyValues();

    /**
     * bean 作用域
     *
     * @since 0.0.2
     */
    private String scope = SCOPE_SINGLETON;

    /**
     * 构造函数参数
     *
     * @since 0.0.3
     */
    private ConstructorArgumentValues constructorArgumentValues;

    /**
     * bean 初始化回调的方法名称
     *
     * @since 0.0.3
     */
    private String initMethodName;

    public BeanDefinition() {

    }

    public BeanDefinition(Class<?> beanClass) {
        setBeanClass(beanClass);
    }

    /**
     * bean 的作用域是否是单例
     *
     * @see #SCOPE_SINGLETON
     * @since 0.0.2
     */
    public boolean isSingleton() {
        return SCOPE_SINGLETON.equals(this.scope);
    }

    /**
     * bean 的作用域是否原型
     *
     * @see #SCOPE_PROTOTYPE
     * @since 0.0.2
     */
    public boolean isPrototype() {
        return SCOPE_PROTOTYPE.equals(this.scope);
    }

    /**
     * bean 是否有构造函数参数
     *
     * @since 0.0.3
     */
    public boolean hasConstructorArgumentValues() {
        return (this.constructorArgumentValues != null && !this.constructorArgumentValues.isEmpty());
    }

}

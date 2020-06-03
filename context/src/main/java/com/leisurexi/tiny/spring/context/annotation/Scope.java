package com.leisurexi.tiny.spring.context.annotation;

import java.lang.annotation.*;

import static com.leisurexi.tiny.spring.beans.factory.support.BeanDefinition.SCOPE_SINGLETON;

/**
 * 用于修饰 bean 的作用域，跟 XML bean 标签的 scope 属性
 * 作用一样
 *
 * @author: leisurexi
 * @date: 2020-05-31 21:47
 * @since 0.0.4
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Scope {

    String value() default SCOPE_SINGLETON;

}

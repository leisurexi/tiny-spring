package com.leisurexi.tiny.spring.beans.factory.config;

import com.leisurexi.tiny.spring.beans.factory.ObjectFactory;

/**
 * 自定义 bean 作用域扩展接口
 *
 * @author: leisurexi
 * @date: 2020-04-29 22:46
 * @since 0.0.2
 */
public interface Scope {

    /**
     * 作用域名称
     *
     * @return 自定义的作用域名称
     */
    String scopeName();

    /**
     * 从作用域返回给定名称的 bean
     *
     * @param name          bean名称
     * @param objectFactory {@link ObjectFactory} 用于创建作用域对象实例
     * @return 想要的对象
     */
    Object get(String name, ObjectFactory<?> objectFactory);

}

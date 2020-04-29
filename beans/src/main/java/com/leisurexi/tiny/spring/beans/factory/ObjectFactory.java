package com.leisurexi.tiny.spring.beans.factory;

/**
 * 一个可以返回实例的工厂
 *
 * @author: leisurexi
 * @date: 2020-04-29 22:47
 * @since 0.0.2
 */
@FunctionalInterface
public interface ObjectFactory<T> {

    /**
     * 返回一个由该工厂管理的对象实例
     *
     * @return 对象实例
     */
    T getObject();

}

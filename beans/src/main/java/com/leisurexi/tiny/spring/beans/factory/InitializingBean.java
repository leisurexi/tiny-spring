package com.leisurexi.tiny.spring.beans.factory;

/**
 * 在 bean 属性赋值后调用，可以看作是一个 init 方法，属于 bean 的生命周期方法
 *
 * @author: leisurexi
 * @date: 2020-05-20 8:26
 * @since 0.0.3
 */
public interface InitializingBean {

    /**
     * bean 属性设置完后调用
     */
    void afterPropertiesSet();

}

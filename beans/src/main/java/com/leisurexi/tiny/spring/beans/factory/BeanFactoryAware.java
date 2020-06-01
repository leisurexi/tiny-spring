package com.leisurexi.tiny.spring.beans.factory;

/**
 * bean factory 的资源回调接口
 *
 * @author: leisurexi
 * @date: 2020-06-01 22:14
 * @since 0.0.4
 */
public interface BeanFactoryAware extends Aware {

    /**
     * bean factory 资源回调接口
     *
     * @param beanFactory bean 工厂
     */
    void setBeanFactory(AbstractAutowireCapableBeanFactory beanFactory);

}

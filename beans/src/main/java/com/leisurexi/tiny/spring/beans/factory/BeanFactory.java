package com.leisurexi.tiny.spring.beans.factory;

/**
 * 抽象 Bean 工厂
 *
 * @author: leisurexi
 * @date: 2020-04-04 14:20
 * @since 0.0.1
 */
public interface BeanFactory {

    /**
     * 按照名称获取 bean
     *
     * @param name bean 名称
     * @return bean 实例
     */
    Object getBean(String name);

}

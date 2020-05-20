package com.leisurexi.tiny.spring.beans.factory.config;

import lombok.Getter;
import lombok.Setter;

/**
 * 依赖描述符
 *
 * @author: leisurexi
 * @date: 2020-05-02 11:18 上午
 * @since 0.0.3
 */
@Getter
@Setter
public class DependencyDescriptor {

    /**
     * 申明的 bean 类型，即需要依赖注入的 bean 类型
     */
    private final Class<?> declaringClass;

    /**
     * 参数类型
     */
    private Class<?> parameterTypes;

    /**
     * 参数名称
     */
    private String parameterName;

    /**
     * 参数下标
     */
    private int parameterIndex;

    public DependencyDescriptor(Class<?> declaringClass, Class<?> parameterTypes, String parameterName) {
        this.declaringClass = declaringClass;
        this.parameterTypes = parameterTypes;
        this.parameterName = parameterName;
    }

    public DependencyDescriptor(Class<?> declaringClass, Class<?> parameterTypes, String parameterName, int parameterIndex) {
        this.declaringClass = declaringClass;
        this.parameterTypes = parameterTypes;
        this.parameterName = parameterName;
        this.parameterIndex = parameterIndex;
    }

    public Class<?> getDependencyType() {
        return parameterTypes;
    }

}

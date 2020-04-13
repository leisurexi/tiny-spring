package com.leisurexi.tiny.spring.beans;

import lombok.ToString;

/**
 * 单个 Bean 属性名和值的存储对象
 *
 * @author: leisurexi
 * @date: 2020-04-04 3:36 下午
 * @since 0.0.1
 */
@ToString
public class PropertyValue {

    /**
     * 属性名
     */
    private final String name;

    /**
     * 属性值
     */
    private final Object value;

    public PropertyValue(String name, Object value) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }
}

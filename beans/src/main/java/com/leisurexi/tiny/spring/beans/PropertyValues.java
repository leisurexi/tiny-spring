package com.leisurexi.tiny.spring.beans;

import lombok.ToString;

import java.util.*;
import java.util.stream.Stream;

/**
 * 包含一个或多个 {@link PropertyValue}
 *
 * @author: leisurexi
 * @date: 2020-04-04 3:44 下午
 * @since 0.0.1
 */
@ToString
public class PropertyValues implements Iterable<PropertyValue> {

    private List<PropertyValue> propertyValues;

    public PropertyValues() {
        this.propertyValues = new ArrayList<>(0);
    }

    /**
     * 添加属性
     */
    public void addPropertyValues(PropertyValue propertyValue) {
        this.propertyValues.add(propertyValue);
    }

    /**
     * 获取全部属性
     */
    public List<PropertyValue> getPropertyValues() {
        return this.propertyValues;
    }

    /**
     * 返回 PropertyValue 的 {@link Iterable}
     */
    @Override
    public Iterator<PropertyValue> iterator() {
        return getPropertyValues().iterator();
    }

    /**
     * 返回 PropertyValue 的 {@link Spliterator}
     */
    @Override
    public Spliterator<PropertyValue> spliterator() {
        return Spliterators.spliterator(getPropertyValues(), 0);
    }

    /**
     * 返回 PropertyValue 的 {@link Stream}
     */
    public Stream<PropertyValue> stream() {
        return getPropertyValues().stream();
    }

    /**
     * 是否包含此属性
     *
     * @param propertyName 属性名称
     * @return {@code true} 包含此属性，{@code false} 不包含此属性
     */
    public boolean contains(String propertyName) {
        for (PropertyValue propertyValue : this.propertyValues) {
            if (propertyValue.getName().equals(propertyName)) {
                return true;
            }
        }
        return false;
    }

}

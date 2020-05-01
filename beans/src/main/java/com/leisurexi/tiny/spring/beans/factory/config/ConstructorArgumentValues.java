package com.leisurexi.tiny.spring.beans.factory.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 构造函数参数保存容器
 *
 * @author: leisurexi
 * @date: 2020-05-01 10:56
 * @since 0.0.3
 */
public class ConstructorArgumentValues {

    private final Map<Integer, Object> indexedArgumentsValues = new LinkedHashMap<>();

    public void addIndexArgumentValue(int index, Object value) {
        this.indexedArgumentsValues.put(index, value);
    }

    public Object getArgumentValue(int index) {
        return this.indexedArgumentsValues.get(index);
    }

    public boolean isEmpty() {
        return this.indexedArgumentsValues.isEmpty();
    }

    public int getArgumentCount() {
        return this.indexedArgumentsValues.size();
    }

    public Map<Integer, Object> getArgumentsValues() {
        return this.indexedArgumentsValues;
    }

}

package com.leisurexi.tiny.spring.beans.scope;

import com.leisurexi.tiny.spring.beans.factory.ObjectFactory;
import com.leisurexi.tiny.spring.beans.factory.config.Scope;

import java.util.HashMap;
import java.util.Map;

/**
 * ThreadLocal 级别的作用域
 *
 * @author: leisurexi
 * @date: 2020-04-29 23:17
 * @since 0.0.2
 */
public class ThreadLocalScope implements Scope {

    private ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal() {
        // 兜底实现，防止空指针
        @Override
        protected Object initialValue() {
            return new HashMap<>();
        }
    };

    @Override
    public String scopeName() {
        return "thread-local";
    }

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        Map<String, Object> map = threadLocal.get();
        Object bean = map.get(name);
        if (bean == null) {
            bean = objectFactory.getObject();
            map.put(name, bean);
        }
        return bean;
    }

}

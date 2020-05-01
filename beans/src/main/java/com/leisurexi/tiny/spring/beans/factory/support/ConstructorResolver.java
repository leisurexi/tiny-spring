package com.leisurexi.tiny.spring.beans.factory.support;

import cn.hutool.core.convert.Convert;
import com.leisurexi.tiny.spring.beans.exception.BeansException;
import com.leisurexi.tiny.spring.beans.factory.AbstractAutowireCapableBeanFactory;
import com.leisurexi.tiny.spring.beans.factory.config.ConstructorArgumentValues;

import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * 通过参数匹配，找到构造器并执行创建实例
 *
 * @author: leisurexi
 * @date: 2020-05-01 11:15
 * @since 0.0.3
 */
public class ConstructorResolver {

    private static final Object[] EMPTY_ARGS = new Object[0];

    private AbstractAutowireCapableBeanFactory beanFactory;

    public ConstructorResolver(AbstractAutowireCapableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * 自动装配构造函数
     *
     * @param beanName       bean 的名称
     * @param beanDefinition bean 的定义元信息
     * @return bean 的实例
     * @since 0.0.3
     */
    public Object autowireConstructor(String beanName, BeanDefinition beanDefinition) {
        // 最终实例化的构造函数
        Constructor<?> constructorToUse = null;
        // 最终用于实例化的构造函数参数
        Object[] argsToUse = null;
        Class<?> beanClass = beanDefinition.getBeanClass();
        // 获取所有的构造器
        Constructor<?>[] candidates = beanClass.getDeclaredConstructors();
        // 定义了构造器参数
        if (beanDefinition.hasConstructorArgumentValues()) {
            ConstructorArgumentValues cargs = beanDefinition.getConstructorArgumentValues();
            ConstructorArgumentValues resolvedValues = new ConstructorArgumentValues();
            int minNrOfArgs = cargs.getArgumentCount();
            resolveConstructorArguments(beanName, beanDefinition, cargs, resolvedValues);
            for (Constructor<?> candidate : candidates) {
                if (candidate.getParameterCount() == minNrOfArgs) {
                    constructorToUse = candidate;
                    argsToUse = createArgumentArray(candidate.getParameterTypes(), resolvedValues);
                }
            }
        }
        // 没有找到匹配的构造函数，抛出异常
        if (constructorToUse == null) {
            throw new IllegalStateException("Could not find matching constructor");
        }
        if (argsToUse == null) {
            throw new IllegalStateException("Unresolved constructor arguments");
        }
        return instantiate(constructorToUse, argsToUse);
    }

    /**
     * 解析构造参数，如果是引用别的 bean 会通过 getBean 操作获取实例
     *
     * @param beanName       bean 的名称
     * @param beanDefinition bean 的定义元信息
     * @param cargs          构造器参数
     * @param resolvedValues 解析过后的构造器参数
     */
    private void resolveConstructorArguments(String beanName, BeanDefinition beanDefinition, ConstructorArgumentValues cargs, ConstructorArgumentValues resolvedValues) {
        BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this.beanFactory);
        for (Map.Entry<Integer, Object> entry : cargs.getArgumentsValues().entrySet()) {
            Object value = valueResolver.resolveValueIfNecessary("constructor argument", entry.getValue());
            resolvedValues.addIndexArgumentValue(entry.getKey(), value);
        }
    }

    /**
     * 对构造器参数进行转换
     *
     * @param paramTypes     构造器参数类型
     * @param resolvedValues 解析过后的值
     * @return 类型转换过后的构造器参数数组
     */
    private Object[] createArgumentArray(Class<?>[] paramTypes, ConstructorArgumentValues resolvedValues) {
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            Object argumentValue = Convert.convert(paramTypes[i], resolvedValues.getArgumentValue(i));
            args[i] = argumentValue;
        }
        return args;
    }

    private Object instantiate(Constructor<?> ctor, Object... args) {
        try {
            return ctor.newInstance(args);
        } catch (Exception e) {
            throw new BeansException(e);
        }
    }

}

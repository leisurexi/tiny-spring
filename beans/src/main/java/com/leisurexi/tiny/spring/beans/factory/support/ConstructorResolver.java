package com.leisurexi.tiny.spring.beans.factory.support;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ArrayUtil;
import com.leisurexi.tiny.spring.beans.exception.BeansException;
import com.leisurexi.tiny.spring.beans.factory.AbstractAutowireCapableBeanFactory;
import com.leisurexi.tiny.spring.beans.factory.config.ConstructorArgumentValues;
import com.leisurexi.tiny.spring.beans.factory.config.DependencyDescriptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static com.leisurexi.tiny.spring.beans.factory.support.BeanDefinition.AUTOWIRE_CONSTRUCTOR;

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
        // 是否是构造器自动注入模式
        boolean autowiring = beanDefinition.getAutowireMode() == AUTOWIRE_CONSTRUCTOR;

        ConstructorArgumentValues cargs = beanDefinition.getConstructorArgumentValues();
        ConstructorArgumentValues resolvedValues = new ConstructorArgumentValues();
        int minNrOfArgs = cargs.getArgumentCount();
        resolveConstructorArguments(beanName, beanDefinition, cargs, resolvedValues);

        for (Constructor<?> candidate : candidates) {
            argsToUse = createArgumentArray(beanName, beanDefinition.getBeanClass(), candidate.getParameterTypes(), getParameters(candidate), resolvedValues, autowiring);

        }

//        // 定义了构造器参数
//        if (beanDefinition.hasConstructorArgumentValues()) {
//
//            for (Constructor<?> candidate : candidates) {
//                if (candidate.getParameterCount() == minNrOfArgs) {
//
//                }
//            }
//        }
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
     * 获取函数的参数名称
     */
    private String[] getParameters(Constructor<?> constructor) {
        Parameter[] parameters = constructor.getParameters();
        String[] paramNames = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            paramNames[i] = parameters[i].getName();
        }
        return paramNames;
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
     * @param beanName       bean 的名称
     * @param beanClass      bean 的类型
     * @param paramTypes     构造器参数类型
     * @param paramNames     构造器参数名
     * @param resolvedValues 解析过后的值
     * @return 类型转换过后的构造器参数数组
     */
    private Object[] createArgumentArray(String beanName, Class<?> beanClass, Class<?>[] paramTypes, String[] paramNames, ConstructorArgumentValues resolvedValues, boolean autowiring) {
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            Object argumentValue = Convert.convert(paramType, resolvedValues.getArgumentValue(i));
            if (argumentValue != null) {
                // 找到了指定构造函数的参数值
                args[i] = argumentValue;
            } else {
                // 没有找到指定的参数值，并且不是自动注入模式，抛出异常
                if (!autowiring) {
                    throw new IllegalStateException("Ambiguous argument values for parameter of type [" + paramType.getName() +
                            "] - did you specify the correct bean references as arguments?");
                }
                args[i] = resolveAutowireArgument(beanName, beanClass, i, paramType, paramNames[i]);
            }
        }
        return args;
    }

    /**
     * 解析指定参数自动注入
     *
     * @param beanName       bean 的名称
     * @param beanClass      bean 的类型
     * @param parameterIndex 参数下标
     * @param parameterType  参数类型
     * @return 符合条件的 bean，或者 {@code null} 如果没找到
     */
    private Object resolveAutowireArgument(String beanName, Class<?> beanClass, int parameterIndex, Class<?> parameterType, String parameterName) {
        DependencyDescriptor dependencyDescriptor = new DependencyDescriptor(beanClass, parameterType, parameterName, parameterIndex);
        return this.beanFactory.resolveDependency(dependencyDescriptor, beanName);
    }

    private Object instantiate(Constructor<?> ctor, Object... args) {
        try {
            return ctor.newInstance(args);
        } catch (Exception e) {
            throw new BeansException(e);
        }
    }

}

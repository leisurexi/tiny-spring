package com.leisurexi.tiny.spring.beans.factory.support;

import cn.hutool.core.convert.Convert;
import com.leisurexi.tiny.spring.beans.exception.BeansException;
import com.leisurexi.tiny.spring.beans.factory.AbstractAutowireCapableBeanFactory;
import com.leisurexi.tiny.spring.beans.factory.config.ConstructorArgumentValues;
import com.leisurexi.tiny.spring.beans.factory.config.DependencyDescriptor;
import com.leisurexi.tiny.spring.beans.util.LocalVariableTableParameterNameDiscoverer;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.leisurexi.tiny.spring.beans.factory.support.BeanDefinition.AUTOWIRE_CONSTRUCTOR;

/**
 * 通过参数匹配，找到构造器并执行创建实例
 *
 * @author: leisurexi
 * @date: 2020-05-01 11:15
 * @since 0.0.3
 */
@Slf4j
public class ConstructorResolver {

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

        // 这里对构造函数进行排序，规则是首先是public构造函数且参数个数从多到少，然后是非public构造函数且参数个数有多到少
        Arrays.sort(candidates, (o1, o2) -> {
            int result = Boolean.compare(Modifier.isPublic(o2.getModifiers()), Modifier.isPublic(o1.getModifiers()));
            return result != 0 ? result : Integer.compare(o2.getParameterCount(), o1.getParameterCount());
        });

        for (Constructor<?> candidate : candidates) {
            try {
                // 这里去解析参数，会进行对应的类型转换，如果是引用别的 bean 会进行 getBean() 获取其实例
                // 如果是构造器依赖注入没有找到对应类型的 bean，会抛出异常，去解析下一个构造器的参数
                argsToUse = createArgumentArray(beanName, beanDefinition.getBeanClass(), candidate.getParameterTypes(),
                        getParameters(candidate), resolvedValues, autowiring);
            } catch (BeansException e) {
                // 由于自动注入，有没有找到的 bean 引用
                log.warn("构造器依赖自动注入，有参数没找到，跳过本次循环，进行下一次查找");
                continue;
            }

            if (beanDefinition.hasConstructorArgumentValues() && argsToUse.length == minNrOfArgs) {
                constructorToUse = candidate;
                // 指定了构造器参数，并且数量对应，跳出循环直接使用
                log.debug("根据指定的参数，找到了对应构造器，直接跳出循环使用");
                break;
            } else {
                constructorToUse = candidate;
                // 构造器依赖注入，所需的 bean 全部找到，跳出循环直接使用
                log.debug("构造器依赖自动注入，需要的 bean 全部找到，跳出循环直接使用");
                break;
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
     * 获取函数的参数名称
     */
    private List<String> getParameters(Constructor<?> constructor) {
        return LocalVariableTableParameterNameDiscoverer.getConstructorParamNames(constructor);
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
    private Object[] createArgumentArray(String beanName, Class<?> beanClass, Class<?>[] paramTypes, List<String> paramNames, ConstructorArgumentValues resolvedValues, boolean autowiring) {
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
                Object argument = resolveAutowireArgument(beanName, beanClass, i, paramType, paramNames.get(i));
                if (argument == null) {
                    throw new BeansException("No such bean " + paramNames.get(i));
                }
                args[i] = argument;
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

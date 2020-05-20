package com.leisurexi.tiny.spring.beans.util;

import com.github.houbb.asm.tool.reflection.AsmMethods;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author: leisurexi
 * @date: 2020-05-02 17:39
 * @since 0.0.3
 */
@Slf4j
public class LocalVariableTableParameterNameDiscoverer {

    /**
     * 获取构造器参数名称
     *
     * @param constructor 指定的构造器
     * @return 参数名称集合
     */
    public static List<String> getConstructorParamNames(Constructor<?> constructor) {
        return AsmMethods.getParamNamesByAsm(constructor);
    }

    /**
     * 获取方法参数名称
     *
     * @param method 指定的方法
     * @return 参数名称集合
     */
    public static List<String> getMethodParamNames(Method method) {
        return AsmMethods.getParamNamesByAsm(method);
    }

}

package com.leisurexi.tiny.spring.context.annotation;

import java.lang.annotation.*;

/**
 * @author: leisurexi
 * @date: 2020-06-02 23:37
 * @since 0.0.5
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bean {

    /**
     * bean 的名称
     */
    String name() default "";

    /**
     * 初始化方法名称
     */
    String initMethod() default "";

}

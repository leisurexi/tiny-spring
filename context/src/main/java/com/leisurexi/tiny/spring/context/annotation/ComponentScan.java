package com.leisurexi.tiny.spring.context.annotation;

import java.lang.annotation.*;

/**
 * @author: leisurexi
 * @date: 2020-06-04 0:09
 * @since 0.0.5
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ComponentScan {

    /**
     * 包路径
     */
    String[] basePackages() default {};

}

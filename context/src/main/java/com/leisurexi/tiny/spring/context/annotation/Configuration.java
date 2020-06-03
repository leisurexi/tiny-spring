package com.leisurexi.tiny.spring.context.annotation;

import java.lang.annotation.*;

/**
 * @author: leisurexi
 * @date: 2020-06-02 23:16
 * @since 0.0.5
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Configuration {

    String value() default "";

}

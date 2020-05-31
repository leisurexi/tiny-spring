package com.leisurexi.tiny.spring.context.annotation;

import java.lang.annotation.*;

/**
 * 被修饰的类会被作为组件注册进 beanFactory 中，
 * 基于注解的配置或者包路径扫描时才会生效
 *
 * @author: leisurexi
 * @date: 2020-05-31 21:44
 * @since 0.0.4
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Component {

    /**
     * 该值会被当做组件的名称，默认为类名的首字母小写
     */
    String value() default "";

}

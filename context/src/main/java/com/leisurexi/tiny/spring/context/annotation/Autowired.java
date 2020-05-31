package com.leisurexi.tiny.spring.context.annotation;

import java.lang.annotation.*;

/**
 * 被标注的属性会从 beanFactory 中寻找相同类型的 bean 进行自动注入
 *
 * @author: leisurexi
 * @date: 2020-05-31 22:02
 * @since 0.0.4
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {

}

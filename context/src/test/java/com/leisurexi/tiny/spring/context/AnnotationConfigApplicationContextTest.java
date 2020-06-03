package com.leisurexi.tiny.spring.context;

import com.leisurexi.tiny.spring.context.config.BeanConfig;
import com.leisurexi.tiny.spring.context.service.UserService;
import com.leisurexi.tiny.spring.context.support.AnnotationConfigApplicationContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author: leisurexi
 * @date: 2020-05-31 21:27
 * @since JDK 1.8
 */
@Slf4j
public class AnnotationConfigApplicationContextTest {

    @Test
    public void test() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(BeanConfig.class);
        context.refresh();
        UserService userService = context.getBean(UserService.class);
        userService.save();
    }

}

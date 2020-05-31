package com.leisurexi.tiny.spring.context;

import com.leisurexi.tiny.spring.context.domain.User;
import com.leisurexi.tiny.spring.context.support.AnnotationConfigApplicationContext;
import com.leisurexi.tiny.spring.context.support.ClassPathXmlApplicationContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author: leisurexi
 * @date: 2020-05-31 21:27
 * @since JDK 1.8
 */
@Slf4j
public class ApplicationContextTest {

    @Test
    public void classPathXmlApplicationContextTest() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/application-context-refresh.xml");
        User user = context.getBean("user", User.class);
        log.info(user.toString());
    }

    @Test
    public void annotationConfigApplicationContextTest() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.scan("com.leisurexi.tiny.spring.context");
        context.refresh();
        User user = context.getBean("user", User.class);
        log.info(user.toString());
    }
}

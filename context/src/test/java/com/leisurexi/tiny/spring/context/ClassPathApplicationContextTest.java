package com.leisurexi.tiny.spring.context;

import com.leisurexi.tiny.spring.context.domain.User;
import com.leisurexi.tiny.spring.context.support.ClassPathXmlApplicationContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author: leisurexi
 * @date: 2020-06-02 22:46
 * @since JDK 1.8
 */
@Slf4j
public class ClassPathApplicationContextTest {

    @Test
    public void test() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/classpath-application-context.xml");
        User user = context.getBean("user", User.class);
        log.info(user.toString());
    }

}

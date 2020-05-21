package com.leisurexi.tiny.spring.beans.domain;

import com.leisurexi.tiny.spring.beans.factory.InitializingBean;
import lombok.*;
import lombok.extern.slf4j.Slf4j;


/**
 * @author: leisurexi
 * @date: 2020-04-04 7:39 下午
 * @since 0.0.1
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class User implements InitializingBean {

    private Long id;
    private String name;
    private City city;

    public User(City city) {
        this.id = 2L;
        this.name = "罗大大";
        this.city = city;
    }

    public void init() {
        log.info("init");
    }

    @Override
    public void afterPropertiesSet() {
        log.info("afterPropertiesSet");
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", city=" + city.hashCode() +
                '}';
    }
}

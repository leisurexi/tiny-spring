package com.leisurexi.tiny.spring.context.config;

import com.leisurexi.tiny.spring.context.annotation.Bean;
import com.leisurexi.tiny.spring.context.annotation.ComponentScan;
import com.leisurexi.tiny.spring.context.annotation.Configuration;
import com.leisurexi.tiny.spring.context.domain.City;
import com.leisurexi.tiny.spring.context.domain.User;

/**
 * @author: leisurexi
 * @date: 2020-06-03 23:35
 * @since JDK 1.8
 */
@Configuration
@ComponentScan(basePackages = "com.leisurexi.tiny.spring.context")
public class BeanConfig {

    @Bean
    public City city() {
        City city = City.builder()
                .id(1L)
                .name("北京")
                .build();
        return city;
    }

    @Bean
    public User user(City city) {
        User user = User.builder()
                .id(1L)
                .name("leisurexi")
                .city(city)
                .build();
        return user;
    }

}

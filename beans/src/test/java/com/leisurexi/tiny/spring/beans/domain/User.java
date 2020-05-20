package com.leisurexi.tiny.spring.beans.domain;

import lombok.*;


/**
 * @author: leisurexi
 * @date: 2020-04-04 7:39 下午
 * @since 0.0.1
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private Long id;
    private String name;
    private City city;

    public User(City city) {
        this.id = 2L;
        this.name = "罗大大";
        this.city = city;
    }
}

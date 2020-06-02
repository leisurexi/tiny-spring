package com.leisurexi.tiny.spring.context.domain;

import com.leisurexi.tiny.spring.context.annotation.Autowired;
import lombok.Data;

/**
 * @author: leisurexi
 * @date: 2020-05-31 21:30
 * @since 0.0.4
 */
@Data
public class User {

    private Long id;
    private String name;

    @Autowired
    private City city;

}

package com.leisurexi.tiny.spring.context.domain;

import com.leisurexi.tiny.spring.context.annotation.Autowired;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: leisurexi
 * @date: 2020-05-31 21:30
 * @since 0.0.4
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private Long id;
    private String name;

    private City city;

}

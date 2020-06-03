package com.leisurexi.tiny.spring.context.domain;

import com.leisurexi.tiny.spring.context.annotation.Component;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: leisurexi
 * @date: 2020-06-01 22:44
 * @since 0.0.4
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class City {

    private Long id;
    private String name;


}

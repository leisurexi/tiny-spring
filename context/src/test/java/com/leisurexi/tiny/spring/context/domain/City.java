package com.leisurexi.tiny.spring.context.domain;

import com.leisurexi.tiny.spring.context.annotation.Component;
import lombok.Data;

/**
 * @author: leisurexi
 * @date: 2020-06-01 22:44
 * @since JDK 1.8
 */
@Data
@Component
public class City {

    private Long id = 1L;
    private String name = "北京";


}

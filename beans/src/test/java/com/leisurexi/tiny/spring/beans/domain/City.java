package com.leisurexi.tiny.spring.beans.domain;

import lombok.*;

/**
 * @author: leisurexi
 * @date: 2020-04-30 0:41
 * @since 0.0.3
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class City {

    private Long id;
    private String name;
    private User user;

    @Override
    public String toString() {
        return "City{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", user=" + (user == null ? null : user.hashCode()) +
                '}';
    }
}

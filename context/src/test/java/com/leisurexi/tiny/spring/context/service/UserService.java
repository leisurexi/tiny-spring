package com.leisurexi.tiny.spring.context.service;

import com.leisurexi.tiny.spring.context.annotation.Autowired;
import com.leisurexi.tiny.spring.context.annotation.Component;
import com.leisurexi.tiny.spring.context.domain.User;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: leisurexi
 * @date: 2020-06-04 0:21
 * @since JDK 1.8
 */
@Component
@Slf4j
public class UserService {

    @Autowired
    private User user;

    private final Map<String, User> repository = new HashMap<>();

    public void save() {
        repository.put(user.getName(), user);
        log.info("所有保存的 user: [{}]", repository);
    }

}

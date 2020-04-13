package com.leisurexi.tiny.spring.beans.exception;

/**
 * @author: leisurexi
 * @date: 2020-04-04 5:13 下午
 * @since JDK 1.8
 */
public class BeansException extends RuntimeException {

    public BeansException(String message) {
        super(message);
    }

    public BeansException(Throwable cause) {
        super(cause);
    }

}

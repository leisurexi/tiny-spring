package com.leisurexi.tiny.spring.beans.io;

import java.net.URL;

/**
 * @author: leisurexi
 * @date: 2020-04-04 7:08 下午
 * @since 0.0.1
 */
public class ResourceLoader {

    public Resource getResource(String location) {
        URL resource = this.getClass().getClassLoader().getResource(location);
        return new UrlResource(resource);
    }
}

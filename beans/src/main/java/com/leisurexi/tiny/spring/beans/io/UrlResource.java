package com.leisurexi.tiny.spring.beans.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author: leisurexi
 * @date: 2020-04-04 7:06 下午
 * @since 0.0.1
 */
public class UrlResource implements Resource {

    private final URL url;

    public UrlResource(URL url) {
        this.url = url;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        URLConnection connection = url.openConnection();
        connection.connect();
        return connection.getInputStream();
    }

}

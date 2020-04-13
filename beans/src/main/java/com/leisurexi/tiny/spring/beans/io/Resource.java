package com.leisurexi.tiny.spring.beans.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * 定位资源接口
 *
 * @author: leisurexi
 * @date: 2020-04-04 7:05 下午
 * @since 0.0.1
 */
public interface Resource {

    InputStream getInputStream() throws IOException;

}

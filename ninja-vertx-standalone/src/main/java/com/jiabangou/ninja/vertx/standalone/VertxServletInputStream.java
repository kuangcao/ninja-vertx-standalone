package com.jiabangou.ninja.vertx.standalone;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * VertxServletInputStream
 * Created by freeway on 16/8/18.
 */
public class VertxServletInputStream extends ServletInputStream {

    private ByteArrayInputStream byteArrayInputStream;

    public VertxServletInputStream(ByteArrayInputStream byteArrayInputStream) {
        this.byteArrayInputStream = byteArrayInputStream;
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener readListener) {

    }

    @Override
    public int read() throws IOException {
        return this.byteArrayInputStream.read();
    }
}

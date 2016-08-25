package com.jiabangou.ninja.vertx.standalone;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.IOException;

/**
 * VertxServletOutputStream
 * Created by freeway on 16/8/18.
 */
public class VertxServletOutputStream extends ServletOutputStream {

    private HttpServerResponse resp;
    private Buffer buffer;

    public VertxServletOutputStream(HttpServerResponse resp) {
        this.resp = resp;
        this.buffer = Buffer.buffer();
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
    }

    @Override
    public void write(int b) throws IOException {
        buffer.appendByte((byte)b);
    }


    public void flush() throws IOException {
        if (buffer.length() > 0) {
            resp.write(buffer);
            buffer = Buffer.buffer();
        }
    }

    public void close() throws IOException {
        if (!resp.ended()) {
            resp.end(buffer);
        }
    }
}

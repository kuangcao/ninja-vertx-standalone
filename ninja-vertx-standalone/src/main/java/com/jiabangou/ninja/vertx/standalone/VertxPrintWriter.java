package com.jiabangou.ninja.vertx.standalone;

import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Created by freeway on 16/8/18.
 */
public class VertxPrintWriter extends PrintWriter {

    public VertxPrintWriter(OutputStream out) {
        super(out);
    }
}

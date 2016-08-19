package com.jiabangou.ninja.vertx.standalone;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import ninja.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ninja Verticale
 * Created by freeway on 16/8/17.
 */
public class NinjaVerticle extends AbstractVerticle {

    static final private Logger log = LoggerFactory.getLogger(NinjaVerticle.class);

    private static Bootstrap bootstrap;

    public static void setBootstrap(Bootstrap bootstrap) {
        NinjaVerticle.bootstrap = bootstrap;
    }

    private static int port = 8080;

    public static void setPort(int port) {
        NinjaVerticle.port = port;
    }

    protected HttpServer httpServer;


    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);
        router.route().handler(NinjaHandler.create(bootstrap));
        httpServer = vertx.createHttpServer();
        httpServer.requestHandler(router::accept).listen(port, res -> {
            if (res.succeeded()) {
                log.info("Server is now listening. Thread:" + Thread.currentThread());
            } else {
                log.info("Failed to bind. Thread:" + Thread.currentThread(), res.cause());
            }
        });
    }

    @Override
    public void stop() throws Exception {
        if (httpServer != null) {
            httpServer.close(res -> {
                if (res.succeeded()) {
                    log.info("Server is closed. Thread:" + Thread.currentThread());
                } else {
                    log.info("Failed to close. Thread:" + Thread.currentThread(), res.cause());
                }
            });
        }
    }

}

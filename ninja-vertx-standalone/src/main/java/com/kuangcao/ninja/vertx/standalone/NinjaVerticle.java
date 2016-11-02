package com.kuangcao.ninja.vertx.standalone;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import ninja.standalone.AbstractStandalone;
import ninja.utils.NinjaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * Ninja Verticale
 * Created by freeway on 16/8/17.
 */
public class NinjaVerticle extends AbstractVerticle {

    static final private Logger log = LoggerFactory.getLogger(NinjaVerticle.class);

    @Inject
    private NinjaHandler ninjaHandler;
    @Inject
    private NinjaProperties ninjaProperties;
    @Inject
    private NinjaVertxBootstrap bootstrap;
    @Inject
    private Map<Class, Object> eventbusMap;
    @Inject
    private Provider<Jedis> provider;


    private int getPort() {
        return ninjaProperties.getInteger(AbstractStandalone.KEY_NINJA_PORT);
    }

    protected HttpServer httpServer;

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);
        VertxEventbus vertxEventbus = VertxEventbus.build(router,vertx,eventbusMap,provider);
        bootstrap.getInjector().getInstance(VertxRoutes.class).init(vertxEventbus);

        router.route().handler(BodyHandler.create());
        router.route().handler(CookieHandler.create());
        router.route().handler(ninjaHandler);
        httpServer = vertx.createHttpServer();
        httpServer.requestHandler(router::accept).listen(getPort(), res -> {
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

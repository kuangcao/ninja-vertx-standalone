package com.jiabangou.ninja.vertx.standalone;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import ninja.Bootstrap;
import ninja.Context;
import ninja.Ninja;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * NinjaHandler
 * Created by freeway on 16/8/17.
 */
public class NinjaHandler implements Handler<RoutingContext> {


    private static NinjaVertxBootstrap bootstrap;

    public static void setBootstrap(NinjaVertxBootstrap bootstrap) {
        NinjaHandler.bootstrap = bootstrap;
    }

    public NinjaHandler() {
    }

    public static NinjaHandler create() {
        return new NinjaHandler();
    }

    @Override
    public void handle(RoutingContext event) {

        VertxHttpServletRequest request = new VertxHttpServletRequest(event);
        request.setContextPath(bootstrap.getContextPath());
        HttpServletResponse response = new VertxHttpServletResponse(event);

        // We generate a Ninja compatible context element
        NinjaVertxServletContext context = (NinjaVertxServletContext) bootstrap
                .getInjector().getProvider(Context.class).get();

        // And populate it
        context.init(null, request, response);

        Ninja ninja = bootstrap.getInjector().getInstance(Ninja.class);
        // And invoke ninja on it.
        // Ninja handles all defined routes, filters and much more:
        ninja.onRouteRequest(context);
    }

}

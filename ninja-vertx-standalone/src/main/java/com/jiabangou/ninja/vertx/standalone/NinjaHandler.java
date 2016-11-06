package com.jiabangou.ninja.vertx.standalone;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import ninja.Context;
import ninja.Ninja;

import javax.servlet.http.HttpServletResponse;

/**
 * NinjaHandler
 * Created by freeway on 16/8/17.
 */
public class NinjaHandler implements Handler<RoutingContext> {

    @Inject
    private NinjaVertxBootstrap bootstrap;

    @Inject
    private Ninja ninja;

    @Inject
    private Provider<Context> contextProvider;

    @Override
    public void handle(RoutingContext event) {
        VertxHttpServletRequest request = new VertxHttpServletRequest(event);
        request.setContextPath(bootstrap.getContextPath());
        HttpServletResponse response = new VertxHttpServletResponse(event);

        // We generate a Ninja compatible context element
        NinjaVertxServletContext context = (NinjaVertxServletContext) contextProvider.get();

        // And populate it
        context.init(null, request, response);

        // And invoke ninja on it.
        // Ninja handles all defined routes, filters and much more:
        ninja.onRouteRequest(context);
    }

}

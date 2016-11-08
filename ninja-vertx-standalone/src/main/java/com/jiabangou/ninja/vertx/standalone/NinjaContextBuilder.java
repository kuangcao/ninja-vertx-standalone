package com.jiabangou.ninja.vertx.standalone;

import io.vertx.ext.web.RoutingContext;
import ninja.Context;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;

/**
 * RoutingContext 转换成 Context
 * Created by freeway on 2016/11/8.
 */
public class NinjaContextBuilder {

    private final Provider<Context>  context;
    private final NinjaVertxBootstrap bootstrap;

    @Inject
    public NinjaContextBuilder(Provider<Context> context, NinjaVertxBootstrap bootstrap) {
        this.context = context;
        this.bootstrap = bootstrap;
    }

    public Context build(RoutingContext event) {
        VertxHttpServletRequest request = new VertxHttpServletRequest(event);
        request.setContextPath(bootstrap.getContextPath());
        HttpServletResponse response = new VertxHttpServletResponse(event);
        // We generate a Ninja compatible context element
        NinjaVertxServletContext ninjaVertxServletContext = (NinjaVertxServletContext) context.get();
        // And populate it
        ninjaVertxServletContext.init(null, request, response);
        return ninjaVertxServletContext;
    }
}

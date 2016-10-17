package com.jiabangou.ninja.vertx.standalone;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public interface VertxRoutes {

    void init(Router router, Vertx vertx);

}

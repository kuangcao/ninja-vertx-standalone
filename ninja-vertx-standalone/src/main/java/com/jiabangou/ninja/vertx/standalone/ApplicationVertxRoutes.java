package com.jiabangou.ninja.vertx.standalone;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

/**
 * 用于封装Vertx中的东西
 * Created by freeway on 2016/11/3.
 */
public interface ApplicationVertxRoutes {

    void init(Router router, Vertx vertx);
}

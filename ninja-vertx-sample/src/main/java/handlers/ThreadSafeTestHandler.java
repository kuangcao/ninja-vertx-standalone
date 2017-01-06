package handlers;

import com.google.inject.Inject;
import com.jiabangou.ninja.vertx.standalone.NinjaContextBuilder;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import ninja.Context;
import ninja.session.Session;

/**
 * 带有cookie验证的逻辑
 * Created by freeway on 2016/11/7.
 */
public class ThreadSafeTestHandler implements Handler<RoutingContext> {


    @Inject
    private Vertx vertx;

    @Inject
    private NinjaContextBuilder ninjaContextBuilder;

    private int i = 0;

    @Override
    public void handle(RoutingContext event) {

        HttpServerResponse response = event.response();
        response.putHeader("content-type", "text/plain");

        // Write to the response and end it
        response.end("i=" + i++);

    }
}

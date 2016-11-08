package handlers;

import com.google.inject.Inject;
import com.jiabangou.ninja.vertx.standalone.NinjaContextBuilder;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.handler.sockjs.impl.SockJSHandlerImpl;
import ninja.Context;
import ninja.session.Session;

/**
 * 带有cookie验证的逻辑
 * Created by freeway on 2016/11/7.
 */
public class SockJSNinjaAuthHandler extends SockJSHandlerImpl {


    private final Vertx vertx;

    private final NinjaContextBuilder ninjaContextBuilder;

    @Inject
    public SockJSNinjaAuthHandler(Vertx vertx, NinjaContextBuilder ninjaContextBuilder) {
        super(vertx, new SockJSHandlerOptions());
        this.vertx = vertx;
        this.ninjaContextBuilder = ninjaContextBuilder;
    }

    @Override
    public void handle(RoutingContext event) {
        Context context = ninjaContextBuilder.build(event);
        Session session = context.getSession();
        session.put("hehe", Thread.currentThread().getName());
        session.save(context);

        super.handle(event);
    }
}

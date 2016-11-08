package handlers;

import com.google.inject.Inject;
import com.jiabangou.ninja.vertx.standalone.NinjaContextBuilder;
import io.vertx.core.Handler;
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
public class AuthHandler implements Handler<RoutingContext> {


    @Inject
    private Vertx vertx;

    @Inject
    private NinjaContextBuilder ninjaContextBuilder;

    @Override
    public void handle(RoutingContext event) {
        Context context = ninjaContextBuilder.build(event);
        Session session = context.getSession();
        session.put("freeway", Thread.currentThread().getName());
        session.save(context);
        //TODO: 验证 session 合法性, 如不符合要求就跳转到验证失败的页面
        event.next();
    }
}

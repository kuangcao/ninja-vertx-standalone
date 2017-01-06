package conf;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.jiabangou.ninja.vertx.standalone.ApplicationVertxRoutes;
import com.jiabangou.ninja.vertx.standalone.NinjaHandler;
import handlers.AuthHandler;
import handlers.Chat2Handler;
import handlers.ChatHandler;
import handlers.ThreadSafeTestHandler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public class VertxRoutes implements ApplicationVertxRoutes {

    //PS: 所有的Handler都需要用Provider, 否则会有线程安全的问题
    @Inject
    private Provider<ChatHandler> chatHandler;
    @Inject
    private Provider<Chat2Handler> chat2Handler;
    @Inject
    private Provider<AuthHandler> authHandler;

    @Inject
    private Provider<NinjaHandler> ninjaHandlerProvider;

    // 不能直接用Handler
    @Inject
    private ThreadSafeTestHandler threadSafeTestHandler;

    @Override
    public void init(Router router, Vertx vertx) {
        //直接用Handler会存在线程安全问题
        router.route("/threadsafe.txt").handler(threadSafeTestHandler);
        router.route().blockingHandler(ninjaHandlerProvider.get(),false);
    }

}

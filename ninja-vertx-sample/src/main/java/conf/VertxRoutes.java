package conf;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.jiabangou.ninja.vertx.standalone.ApplicationVertxRoutes;
import handlers.Chat2Handler;
import handlers.ChatHandler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

public class VertxRoutes implements ApplicationVertxRoutes {

    @Inject
    private Provider<ChatHandler> chatHandler;
    @Inject
    private Provider<Chat2Handler> chat2Handler;

    @Override
    public void init(Router router, Vertx vertx) {

        router.route("/eventbus/*").handler(SockJSHandler.create(vertx).bridge(
                new BridgeOptions()
                        .addInboundPermitted(new PermittedOptions().setAddress("chat.to.server"))
                        .addInboundPermitted(new PermittedOptions().setAddress("chat_to_server"))
                        .addOutboundPermitted(new PermittedOptions().setAddressRegex(".*"))
        ));

        // for sockjs
        EventBus eb = vertx.eventBus();
        eb.consumer("chat.to.server").handler(chatHandler.get());
        eb.consumer("chat_to_server").handler(chat2Handler.get());

    }

}

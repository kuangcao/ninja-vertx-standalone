package conf;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.jiabangou.ninja.vertx.standalone.ApplicationVertxRoutes;
import handlers.AuthHandler;
import handlers.Chat2Handler;
import handlers.ChatHandler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

import java.text.DateFormat;
import java.time.Instant;
import java.util.Date;

public class VertxRoutes implements ApplicationVertxRoutes {

    //PS: 所有的Handler都需要用Provider, 否则会有线程安全的问题
    @Inject
    private Provider<ChatHandler> chatHandler;
    @Inject
    private Provider<Chat2Handler> chat2Handler;
    @Inject
    private Provider<AuthHandler> authHandler;
    @Inject
    private PubSub pubSub;

    @Override
    public void init(Router router, Vertx vertx) {

        router.route("/eventbus/*").handler(authHandler.get());
        SockJSHandlerOptions sockJSHandlerOptions = new SockJSHandlerOptions().setHeartbeatInterval(15000);
        router.route("/eventbus/*")
                .handler(SockJSHandler.create(vertx, sockJSHandlerOptions).bridge(
                new BridgeOptions()
                        .addInboundPermitted(new PermittedOptions().setAddress("chat.to.server"))
                        .addInboundPermitted(new PermittedOptions().setAddress("chat_to_server"))
                        .addOutboundPermitted(new PermittedOptions().setAddressRegex(".*"))
        ));
        initRedisMix(router, vertx);
    }


    private void initNormal(Router router, Vertx vertx) {
        // for sockjs
        EventBus eb = vertx.eventBus();
        eb.consumer("chat.to.server").handler(chatHandler.get());
        eb.consumer("chat_to_server").handler(chat2Handler.get());
    }

    private void initRedis(Router router, Vertx vertx) {

        EventBus eb = vertx.eventBus();
        RedisOptions config = new RedisOptions()
                .setHost("localhost");

        RedisClient redis = RedisClient.create(vertx, config);
        eb.consumer("chat.to.server").handler(message -> {

            String timestamp = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
                    .format(Date.from(Instant.now()));
            redis.publish("chat.to.client", timestamp + ": " + String.valueOf(message.body()),
                    event -> System.out.println("publish count:" + event.result()));
        });

        eb.consumer("chat_to_server").handler(message -> {
            redis.publish("chat_to_client" + "/" + message.headers().get("channel"),
                    String.valueOf(message.body()), null);
        });

        // 尚未找到断线重连的方法
        vertx.eventBus().<JsonObject>consumer("io.vertx.redis.*", received -> {
            JsonObject value = received.body().getJsonObject("value");
            eb.publish(value.getString("channel"), value.getString("message"));
        });

        RedisClient redisSub = RedisClient.create(vertx, config);
        redisSub.psubscribe("*", handler -> {
            JsonArray jsonArray = handler.result();
            String message = jsonArray.toString();
            System.out.println(message);
        });
    }

    private void initRedisMix(Router router, Vertx vertx) {

        pubSub.startSubscribe();

        EventBus eb = vertx.eventBus();
        eb.consumer("chat.to.server").handler(message -> {
            String timestamp = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
                    .format(Date.from(Instant.now()));
            pubSub.publish("chat.to.client", timestamp + ": " + String.valueOf(message.body()));
        });

        eb.consumer("chat_to_server").handler(message -> {
            pubSub.publish("chat_to_client" + "/" + message.headers().get("channel"),
                    String.valueOf(message.body()));
        });
    }

}

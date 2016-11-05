package conf;

import com.jiabangou.ninja.vertx.standalone.ApplicationVertxRoutes;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.text.DateFormat;
import java.time.Instant;
import java.util.Date;

public class VertxRoutes implements ApplicationVertxRoutes {

    @Override
    public void init(Router router, Vertx vertx) {

        router.route("/eventbus/*").handler(SockJSHandler.create(vertx).bridge(
                new BridgeOptions()
                        .addInboundPermitted(new PermittedOptions().setAddress("chat.to.server"))
                        .addOutboundPermitted(new PermittedOptions().setAddress("chat.to.client"))
                        .addInboundPermitted(new PermittedOptions().setAddress("chat_to_server"))
                        .addInboundPermitted(new PermittedOptions().setAddressRegex("chat_to_server/\\d+"))
                        .addOutboundPermitted(new PermittedOptions().setAddressRegex("chat_to_client/\\d+"))
        ));

        // local node or event bus
        initEventBus(router, vertx);

    }

    private void initEventBus(Router router, Vertx vertx) {

        EventBus eb = vertx.eventBus();
        // local node
        eb.consumer("chat.to.server").handler(message -> {
            // Create a timestamp string
            String timestamp = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
                    .format(Date.from(Instant.now()));
            // Send the message back out to all clients with the timestamp prepended.
            eb.publish("chat.to.client", timestamp + ": " + message.body());
        });
        eb.consumer("chat_to_server").handler(message -> {
            eb.publish("chat_to_client" + "/" + message.headers().get("channel"),
                    String.valueOf(message.body()));
        });

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

        // 支持短线重连
        new Thread(() -> {
            while (true) {
                try {
                    Jedis jedisSub = new Jedis(config.getHost());
                    jedisSub.psubscribe(new JedisPubSub() {
                        public void onPMessage(String pattern, String channel, String message) {
                            eb.publish(channel, message);
                        }

                        public void onPSubscribe(String channel, int subscribedChannels) {
                            System.out.println(String.format("subscribe redis channel success, channel %s, subscribedChannels %d",
                                    channel, subscribedChannels));
                        }

                        public void onPUnsubscribe(String channel, int subscribedChannels) {
                            System.out.println(String.format("unsubscribe redis channel, channel %s, subscribedChannels %d",
                                    channel, subscribedChannels));

                        }
                    }, "*");
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        Thread.sleep(1000l);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }

        }, "vertx-jedis-pubsub").start();

    }

}

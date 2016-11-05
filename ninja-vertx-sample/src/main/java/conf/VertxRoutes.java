package conf;

import com.jiabangou.ninja.vertx.standalone.ApplicationVertxRoutes;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.BridgeEventType;
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
import java.util.HashMap;
import java.util.Map;

public class VertxRoutes implements ApplicationVertxRoutes {

    @Override
    public void init(Router router, Vertx vertx) {

        router.route("/eventbus/*").handler(SockJSHandler.create(vertx).bridge(
                new BridgeOptions()
                        .addInboundPermitted(new PermittedOptions().setAddress("chat.to.server"))
                        .addOutboundPermitted(new PermittedOptions().setAddress("chat.to.client"))
                        .addInboundPermitted(new PermittedOptions().setAddress("chat_to_server"))
                        .addOutboundPermitted(new PermittedOptions().setAddressRegex("chat_to_client/\\d+"))
        ));

        // local node
//        eb.consumer("chat.to.server").handler(message -> {
//            // Create a timestamp string
//            String timestamp = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
//                    .format(Date.from(Instant.now()));
//            System.out.print(message);
//            // Send the message back out to all clients with the timestamp prepended.
//            eb.publish("chat.to.client", timestamp + ": " + message);
//        });

// Jedis for cluster

        Jedis jedis = new Jedis("localhost");
        EventBus eb = vertx.eventBus();
        eb.consumer("chat.to.server").handler(message -> {
            // Create a timestamp string
            String timestamp = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
                    .format(Date.from(Instant.now()));
            jedis.publish("chat.to.client", timestamp + ": " + String.valueOf(message.body()));
        });
        // cunsumer 不支持正则匹配, 所以,发送给server端的channel关于正则的变数保存在header里面, 再通过接收后的逻辑来转
        eb.consumer("chat_to_server").handler(message -> {
            jedis.publish("chat_to_client" + "/" + message.headers().get("channel"),
                    String.valueOf(message.body()));
        });

        Jedis jedisSub = new Jedis("localhost");
        new Thread(() -> {
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

        }, "vertx-jedis-pubsub").start();


        // RedisClient for cluster (订阅不到消息,存在问题, 还没找到原因)
//        RedisOptions config = new RedisOptions()
//                .setHost("localhost");
//
//        RedisClient redis = RedisClient.create(vertx, config);
//
//        eb.consumer("chat.to.server").handler(message -> {
//            redis.publish("chat.to.client", String.valueOf(message.body()),
//                    event -> System.out.println("publish count:" + event.result()));
//        });
//
//        RedisClient redisSub = RedisClient.create(vertx, config);
//        redisSub.subscribe("chat.to.redis", event -> {
//            JsonArray jsonArray = event.result();
//            String message = jsonArray.toString();
//
//            // Create a timestamp string
//            String timestamp = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
//                    .format(Date.from(Instant.now()));
//            System.out.println(message);
//            // Send the message back out to all clients with the timestamp prepended.
//            eb.publish("chat.to.client", timestamp + ": " + message);
//        });


    }

}

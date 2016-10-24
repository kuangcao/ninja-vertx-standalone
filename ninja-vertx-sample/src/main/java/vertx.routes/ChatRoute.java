package vertx.routes;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.text.DateFormat;
import java.time.Instant;
import java.util.Date;

public class ChatRoute implements com.jiabangou.ninja.vertx.standalone.VertxRoutes {



    @Override
    public void init(Router router, Vertx vertx) {
        EventBus eb = vertx.eventBus();
        // Allow events for the designated addresses in/out of the event bus bridge
        BridgeOptions opts = new BridgeOptions()
                .addInboundPermitted(new PermittedOptions().setAddressRegex("chat.to.server"))
                .addOutboundPermitted(new PermittedOptions().setAddressRegex("chat.to.client"));

        // Create the event bus bridge and add it to the router.
        SockJSHandlerOptions options = new SockJSHandlerOptions();//.setHeartbeatInterval(1000);

        SockJSHandler ebHandler = SockJSHandler.create(vertx,options).bridge(opts,e->{
            System.out.println(e.type());
            e.complete(true);
        });



    //    router.route("/myapp/*").handler(ebHandler);

        router.route("/eventbus/*").handler(ebHandler);
//        //  local node
        eb.consumer("chat.to.server").handler(message -> {
            // Create a timestamp string
            String timestamp = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
                    .format(Date.from(Instant.now()));
            System.out.println(message);
            // Send the message back out to all clients with the timestamp prepended.
            eb.publish("chat.to.client", timestamp + ": " + message);
        });

        // Jedis for cluster
        Jedis jedis = new Jedis("localhost",6379);
//        System.out.println("Server is running: "+jedis.ping());
//
        eb.consumer("chat.to.server").handler(message -> {
            jedis.publish("chat.to.server", String.valueOf(message.body()));
        });
//
        Jedis jedisSub = new Jedis("localhost",6379);

        new Thread(()->jedisSub.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                // Create a timestamp string
                String timestamp = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
                        .format(Date.from(Instant.now()));
                System.out.print(message);
                // Send the message back out to all clients with the timestamp prepended.
                eb.publish("chat.to.client", timestamp + ": " + message);
            }

            public void onSubscribe(String channel, int subscribedChannels) {
                System.out.println(String.format("subscribe redis channel success, channel %s, subscribedChannels %d",
                        channel, subscribedChannels));
            }

            public void onUnsubscribe(String channel, int subscribedChannels) {
                System.out.println(String.format("unsubscribe redis channel, channel %s, subscribedChannels %d",
                        channel, subscribedChannels));

            }
        }, "chat.to.server")).start();

//
//
//        // RedisClient for cluster (订阅不到消息,存在问题, 还没找到原因)
//        RedisOptions config = new RedisOptions()
//                .setHost("localhost");
//
//        RedisClient redis = RedisClient.create(vertx, config);
//
//        eb.consumer("chat.to.server").handler(message -> {
//            redis.publish("chat.to.redis", String.valueOf(message.body()),
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

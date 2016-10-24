package vertx.routes;

import com.jiabangou.ninja.vertx.standalone.VertxRoutes;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * Created by wangziqing on 16/10/24.
 */
public class HeartBeatRoute implements VertxRoutes {

    @Override
    public void init(Router router, Vertx vertx) {
        EventBus eb = vertx.eventBus();
        BridgeOptions opts = new BridgeOptions()
                .addInboundPermitted(new PermittedOptions().setAddressRegex("ping"))
                .addOutboundPermitted(new PermittedOptions().setAddressRegex("pong"));
        SockJSHandler ebHandler = SockJSHandler.create(vertx).bridge(opts, e -> {
            System.out.println(e.type());
            e.complete(true);
        });
        router.route("/pingpong/*").handler(ebHandler);

        Jedis jedisPub = new Jedis("localhost", 6379);

        eb.consumer("ping").handler(message ->
            jedisPub.publish("redis.pub.ping", null)
        );

        Jedis jedisConsumer = new Jedis("localhost", 6379);

        new Thread(() -> jedisConsumer.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                eb.publish("pong", System.currentTimeMillis());
            }
        }, "redis.pub.ping")).start();
    }
}

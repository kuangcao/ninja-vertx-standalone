package conf;

import com.google.inject.Inject;
import io.vertx.core.Vertx;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import ninja.utils.NinjaProperties;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * Created by freeway on 2016/11/11.
 */
public class PubSub {

    @Inject
    private Vertx vertx;
    @Inject
    private NinjaProperties ninjaProperties;

    private static final String HOST_KEY = "redis.host";
    private static final String PORT_KEY = "redis.port";

    private volatile static RedisClient redis;
    private RedisClient getInstance() {
        if (redis == null) {
            synchronized (RedisClient.class) {
                RedisOptions config = new RedisOptions()
                        .setHost(ninjaProperties.get(HOST_KEY))
                        .setPort(ninjaProperties.getInteger(PORT_KEY));
                redis = RedisClient.create(vertx, config);
            }
        }
        return redis;
    }

    private RedisClient create() {
        RedisOptions config = new RedisOptions()
                .setHost(ninjaProperties.get(HOST_KEY))
                .setPort(ninjaProperties.getInteger(PORT_KEY));
        return RedisClient.create(vertx, config);
    }

    private Jedis createJedis() {
        return new Jedis(ninjaProperties.get(HOST_KEY), ninjaProperties.getInteger(PORT_KEY));
    }

    public void publish(String address, String message) {
        getInstance().publish(address, message, null);
    }

    public void startSubscribe() {

        // 支持短线重连
        new Thread(() -> {
            while (true) {
                try {
                    Jedis jedisSub = this.createJedis();
                    jedisSub.psubscribe(new JedisPubSub() {
                        public void onPMessage(String pattern, String channel, String message) {
                            vertx.eventBus().publish(channel, message);
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

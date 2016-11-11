package conf;

import com.google.inject.Inject;
import io.vertx.core.Vertx;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import ninja.utils.NinjaProperties;
import redis.clients.jedis.Jedis;

/**
 * Created by freeway on 2016/11/11.
 */
public class RedisClientBuilder {

    @Inject
    private Vertx vertx;
    @Inject
    private NinjaProperties ninjaProperties;

    private static final String HOST_KEY = "redis.host";
    private static final String PORT_KEY = "redis.port";

    private volatile static RedisClient redis;
    public RedisClient getInstance() {
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

    public RedisClient create() {
        RedisOptions config = new RedisOptions()
                .setHost(ninjaProperties.get(HOST_KEY))
                .setPort(ninjaProperties.getInteger(PORT_KEY));
        return RedisClient.create(vertx, config);
    }

    public Jedis createJedis() {
        return new Jedis(ninjaProperties.get(HOST_KEY), ninjaProperties.getInteger(PORT_KEY));
    }

}

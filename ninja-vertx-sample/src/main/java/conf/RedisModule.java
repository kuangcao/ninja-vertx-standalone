package conf;

import com.google.inject.AbstractModule;
import ninja.utils.NinjaProperties;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by wangziqing on 16/10/24.
 */
public class RedisModule extends AbstractModule {

    private final NinjaProperties ninjaProperties;

    private static final String hostKey = "redis.host";
    private static final String portKey = "redis.port";

    public RedisModule(NinjaProperties ninjaProperties) {
        this.ninjaProperties = ninjaProperties;
    }


    @Override
    protected void configure() {
        final String host = ninjaProperties.get(hostKey);
        final Integer port = ninjaProperties.getInteger(portKey);

        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(20);
        config.setMaxIdle(5);
        config.setMaxWaitMillis(1000l);
        config.setTestOnBorrow(false);
        JedisPool pool = new JedisPool(config,host,port,15000);

        bind(Jedis.class).toProvider(pool::getResource);
    }
}

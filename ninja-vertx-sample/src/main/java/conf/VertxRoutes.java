package conf;

import com.kuangcao.ninja.vertx.standalone.VertxEventbus;
import eventbus.ChatEventbus;
import eventbus.TestEventbus;

/**
 * Created by wangziqing on 16/10/31.
 */
public class VertxRoutes implements com.kuangcao.ninja.vertx.standalone.VertxRoutes {
    @Override
    public void init(VertxEventbus eventbus) {
        eventbus.route("/chatroom/*").with(ChatEventbus.class);
        eventbus.route("/eventbus/*").with(TestEventbus.class);
    }

}

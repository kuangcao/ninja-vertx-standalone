package conf;

import com.kuangcao.ninja.vertx.standalone.IVertxRoutes;
import com.kuangcao.ninja.vertx.standalone.VertxEventbus;
import eventbus.Chat2Eventbus;
import eventbus.ChatEventbus;
import eventbus.TestEventbus;

/**
 * Created by wangziqing on 16/10/31.
 */
public class VertxRoutes implements IVertxRoutes {
    @Override
    public void init(VertxEventbus eventbus) {
        eventbus.route("/chatroom/*").with(ChatEventbus.class);
        eventbus.route("/eventbus/*").with(TestEventbus.class);
        eventbus.route("/chatroom2/*").with(Chat2Eventbus.class);
    }

}

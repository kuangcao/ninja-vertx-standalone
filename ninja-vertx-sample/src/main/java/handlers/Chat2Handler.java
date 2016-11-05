package handlers;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

/**
 *
 * Created by freeway on 2016/11/5.
 */
public class Chat2Handler implements Handler<Message<Object>> {

    private Vertx vertx;

    public Chat2Handler setVertx(Vertx vertx) {
        this.vertx = vertx;
        return this;
    }

    @Override
    public void handle(Message<Object> message) {
        vertx.eventBus().publish("chat_to_client" + "/" + message.headers().get("channel"),
                String.valueOf(message.body()));
    }
}

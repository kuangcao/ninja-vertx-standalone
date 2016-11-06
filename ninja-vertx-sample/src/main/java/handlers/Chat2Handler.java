package handlers;

import com.google.inject.Inject;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

/**
 *
 * Created by freeway on 2016/11/5.
 */
public class Chat2Handler implements Handler<Message<Object>> {

    @Inject
    private Vertx vertx;

    @Override
    public void handle(Message<Object> message) {
        vertx.eventBus().publish("chat_to_client" + "/" + message.headers().get("channel"),
                String.valueOf(message.body()));
    }
}

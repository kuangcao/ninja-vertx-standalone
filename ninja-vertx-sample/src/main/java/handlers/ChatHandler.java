package handlers;

import com.google.inject.Inject;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

import java.text.DateFormat;
import java.time.Instant;
import java.util.Date;

/**
 *
 * Created by freeway on 2016/11/5.
 */
public class ChatHandler implements Handler<Message<Object>> {

    @Inject
    private Vertx vertx;

    @Override
    public void handle(Message<Object> message) {
        // Create a timestamp string
        String timestamp = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
                .format(Date.from(Instant.now()));
        // Send the message back out to all clients with the timestamp prepended.
        vertx.eventBus().publish("chat.to.client", timestamp + ": " + message.body());
    }
}

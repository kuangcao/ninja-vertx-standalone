package eventbus;

import com.kuangcao.ninja.vertx.standalone.Permitted;
import com.kuangcao.ninja.vertx.standalone.model.Result;

import java.text.DateFormat;
import java.time.Instant;
import java.util.Date;

/**
 * Created by wangziqing on 16/11/2.
 */
public class ChatEventbus {

    @Permitted(inBound = "chat.to.server", outBound = "chat.to.client")
    public String say(String message){

        String timestamp = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
                .format(Date.from(Instant.now()));
        return "[" + timestamp + "] " + message;
    }

}

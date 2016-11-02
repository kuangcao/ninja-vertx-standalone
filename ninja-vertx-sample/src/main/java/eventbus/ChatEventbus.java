package eventbus;

import com.kuangcao.ninja.vertx.standalone.Permitted;

/**
 * Created by wangziqing on 16/11/2.
 */
public class ChatEventbus {

    @Permitted(inBound = "chat.to.server", outBound = "chat.to.client")
    public String say(String message){
        return message;
    }

}

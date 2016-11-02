package eventbus;

import com.kuangcao.ninja.vertx.standalone.Permitted;
import com.kuangcao.ninja.vertx.standalone.model.Result;

/**
 * Created by wangziqing on 16/11/2.
 */
public class ChatEventbus {

    @Permitted(inBound = "chat.to.server", outBound = "chat.to.client")
    public Result say(String message){

        return Result.build(message);
    }

}

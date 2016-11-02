package eventbus;

import com.kuangcao.ninja.vertx.standalone.Permitted;
import com.kuangcao.ninja.vertx.standalone.model.Result;

/**
 * Created by wangziqing on 16/11/2.
 */
public class Chat2Eventbus {

    @Permitted(inBound = "chat2.to.server.\\d+", outBound = "chat2.to.client.\\d+")
    public Result say(String message){
        return Result.build(message);
    }

}

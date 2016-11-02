package eventbus;

import com.kuangcao.ninja.vertx.standalone.Permitted;

/**
 * Created by wangziqing on 16/11/1.
 */
public class TestEventbus {


    @Permitted(inBound = "com.example:cmd:poke-server", outBound = "com.example:stat:server-info")
    public String test(String message){

        return  message;
    }
}

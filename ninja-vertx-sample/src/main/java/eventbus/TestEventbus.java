package eventbus;

import com.kuangcao.ninja.vertx.standalone.Permitted;
import com.kuangcao.ninja.vertx.standalone.model.Result;

/**
 * Created by wangziqing on 16/11/1.
 */
public class TestEventbus {


    @Permitted(inBound = "com.example:cmd:poke-server", outBound = "com.example:stat:server-info")
    public Result test(String message){
        Test test = new Test();
        test.setName("test");
        return  Result.build(test);
    }

    private class Test{
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}

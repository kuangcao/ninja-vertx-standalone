package conf;

import com.kuangcao.ninja.vertx.standalone.IVertxError;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangziqing on 16/11/2.
 */
public class VertxError implements IVertxError{

    @Override
    public Map errorExcute(Exception e){
//        if(e instanceof ServiceException){
//        }
        Map map = new HashMap();
        map.put("code",10000);
        map.put("message","未知的系统异常");
        return map;
    }
}

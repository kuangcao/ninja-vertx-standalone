package conf;

import com.jiabangou.ninja.vertx.standalone.NinjaVertx;

/**
 * Created by wangziqing on 16/10/20.
 */
public class Run  {
    public static void main(String[] args) {
        new NinjaVertx().run();
      //  System.out.println(PackageScan.getClassList("vertx.routes",false));
    }
}

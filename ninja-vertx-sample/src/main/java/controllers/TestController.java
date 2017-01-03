package controllers;

import com.google.inject.Singleton;
import ninja.Result;
import ninja.Results;
import org.apache.commons.lang.RandomStringUtils;

/**
 * Created by wangziqing on 17/1/1.
 */
@Singleton
public class TestController {

    public Result test() throws InterruptedException {



        Thread.sleep(2000);
        String random = RandomStringUtils.randomNumeric(4);
        System.out.println(random);
        return Results.json().render(random);
    }

    public static void main(String[] args) {
        String random = RandomStringUtils.randomNumeric(4);
        System.out.println(random);
    }
}

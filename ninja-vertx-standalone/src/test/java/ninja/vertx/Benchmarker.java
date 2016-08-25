/*
 * Copyright 2016 joelauer.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ninja.vertx;

import com.google.common.base.Stopwatch;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.jiabangou.ninja.vertx.standalone.NinjaVertx;
import ninja.standalone.NinjaJetty;
import ninja.standalone.Standalone;
import ninja.standalone.StandaloneHelper;
import static ninja.vertx.NinjaOkHttp3Tester.executeRequest;
import static ninja.vertx.NinjaOkHttp3Tester.requestBuilder;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.io.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Benchmarker {
    static private final Logger log = LoggerFactory.getLogger(Benchmarker.class);

    static public void main(String[] args) throws Exception {

        // spin up standalone, but don't join
        Standalone standalone = new NinjaVertx()
//        Standalone standalone = new NinjaJetty()
            .externalConfigurationPath("conf/vertx.example.conf")
            .port(StandaloneHelper.findAvailablePort(8000, 9000))
            .start();
        Thread.sleep(3000);
        final int requests = 100000;
        final int threads = 50;
        
        final OkHttpClient client = NinjaOkHttp3Tester.newHttpClientBuilder()
            .connectionPool(new ConnectionPool(threads, 60000L, TimeUnit.MILLISECONDS))
            .build();
        
        final AtomicInteger requested = new AtomicInteger();
        
        /**
        // get request w/ parameters
        final Request request
            = requestBuilder(standalone, "/parameters?a=joe&c=cat&d=dog&e=egg&f=frank&g=go")
                .header("Cookie", "TEST=THISISATESTCOOKIEHEADER")
                .build();
        */
        
        // json request w/ parameters
        byte[] json = "{ \"s\":\"string\", \"i\":2 }".getBytes(Charsets.UTF_8);
        final Request request
            = requestBuilder(standalone, "/benchmark_json?a=joe&c=cat&d=dog&e=egg&f=frank&g=go")
                .header("Cookie", "TEST=THISISATESTCOOKIEHEADER")
                .post(RequestBody.create(MediaType.parse("application/json"), json))
                .build();
        
        /**
        final Request request
            = requestBuilder(standalone, "/benchmark_form?a=joe&c=cat&d=dog&e=egg&f=frank&g=go")
                .post(new FormBody.Builder()
                    .add("a", "frank")
                    .add("b", "2")
                    .add("h", "hello")
                    .add("z", "zulu")
                    .build())
                .build();
        */
        
        // warmup
        for (int i = 0; i < 100; i++) {
            Response response = executeRequest(client, request);
            response.body().close();
        }
        
        final CountDownLatch startSignal = new CountDownLatch(1);
        final CountDownLatch doneSignal = new CountDownLatch(threads);
        ExecutorService threadPool = Executors.newFixedThreadPool(threads);
        
        for (int i = 0; i < threads; i++) {
            threadPool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        startSignal.await();
                        while (requested.incrementAndGet() < requests) {
                            Response response = executeRequest(client, request);
                            response.body().close();
                        }
                        doneSignal.countDown();
                    } catch (InterruptedException | IOException e) {
                        log.error("", e);
                    }
                }
            });
        }
        
        
        // real
        Stopwatch stopwatch = Stopwatch.createStarted();
        startSignal.countDown();
        doneSignal.await();
        stopwatch.stop();
        log.info("Took {} ms for {} requests", stopwatch.elapsed(TimeUnit.MILLISECONDS), requests);
        logMemory();
        
        standalone.shutdown();
        threadPool.shutdown();
    }
    
    static public void logMemory() {
        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();
         
        log.info("##### Heap utilization statistics [MB] #####");
         
        //Print used memory
        log.info("Used Memory:"
            + (runtime.totalMemory() - runtime.freeMemory()));
 
        //Print free memory
        log.info("Free Memory:"
            + runtime.freeMemory());
         
        //Print total available memory
        log.info("Total Memory:" + runtime.totalMemory());
 
        //Print Maximum available memory
        log.info("Max Memory:" + runtime.maxMemory());
    }
    
}

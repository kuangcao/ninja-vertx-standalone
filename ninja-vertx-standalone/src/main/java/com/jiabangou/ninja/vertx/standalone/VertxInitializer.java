package com.jiabangou.ninja.vertx.standalone;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.jiabangou.ninja.vertx.standalone.guice.GuiceVerticleFactory;
import io.vertx.core.*;
import io.vertx.core.metrics.MetricsOptions;
import ninja.lifecycle.Dispose;
import ninja.lifecycle.Start;
import ninja.utils.NinjaProperties;
import ninja.utils.OverlayedNinjaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * VertxInitializer
 * Created by freeway on 16/8/18.
 */
@Singleton
public class VertxInitializer {

    static final private Logger log = LoggerFactory.getLogger(VertxInitializer.class);
    /**
     * The default number of event loop threads to be used  = 2 * number of cores on the machine
     */
    public static final int DEFAULT_WORKER_POOL_SIZE = 2 * Runtime.getRuntime().availableProcessors();
    public static final String VERTX_IS_WORKER = "vertx.isWorker";

    private DeploymentOptions deploymentOptions;
    private Consumer<Vertx> runner;
    private String verticleID;
    private Vertx vertx;
    private NinjaProperties ninjaProperties;

    @Inject
    public VertxInitializer(NinjaProperties ninjaProperties) {
        this.ninjaProperties = ninjaProperties;
    }


    private void await(CountDownLatch mCountDownLatch) {
        try {
            mCountDownLatch.await();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Start(order = 90)
    public void start() {

        CountDownLatch countDownLatch = new CountDownLatch(1);
        VertxOptions options = new VertxOptions()
                .setMetricsOptions(new MetricsOptions().setEnabled(true));
        deploymentOptions = new DeploymentOptions();
        if (ninjaProperties.getBooleanWithDefault(VERTX_IS_WORKER, true)) {
            deploymentOptions.setWorker(true).setWorkerPoolName("ninja-vertx");
            deploymentOptions.setInstances(
                    ninjaProperties.getIntegerWithDefault("vertx.workerPoolSize", DEFAULT_WORKER_POOL_SIZE));
        }
        verticleID = GuiceVerticleFactory.PREFIX + ":" + NinjaVerticle.class.getName();
        Handler<AsyncResult<String>> handler = stringAsyncResult -> {
            countDownLatch.countDown();
        };
        runner = vertex -> {
            try {
                if (deploymentOptions != null) {
                    vertex.deployVerticle(verticleID, deploymentOptions, handler);
                } else {
                    vertex.deployVerticle(verticleID, handler);
                }
            } catch (Throwable t) {
                log.error(t.getMessage(), t);
            }
        };
        if (options.isClustered()) {
            Vertx.clusteredVertx(options, res -> {
                if (res.succeeded()) {
                    vertx = res.result();
                    runner.accept(vertx);
                } else {
                    log.error(res.cause().getMessage(), res.cause());
                }
            });
            await(countDownLatch);
        } else {
            vertx = Vertx.vertx(options);
            runner.accept(vertx);
            await(countDownLatch);
        }
    }

    @Dispose(order = 5)
    public void stop() {
        vertx.close();
    }


}

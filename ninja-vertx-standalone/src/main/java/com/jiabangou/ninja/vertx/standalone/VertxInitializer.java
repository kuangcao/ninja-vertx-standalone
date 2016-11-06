package com.jiabangou.ninja.vertx.standalone;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.jiabangou.ninja.vertx.standalone.guice.GuiceVerticleFactory;
import io.vertx.core.*;
import io.vertx.core.metrics.MetricsOptions;
import ninja.lifecycle.Dispose;
import ninja.lifecycle.Start;
import ninja.utils.NinjaProperties;
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
    public static final String VERTX_IS_CLUSTERED = "vertx.isClustered";
    public static final String VERTX_IS_METRICS_ENABLED = "vertx.isMetricsEnabled";


    private DeploymentOptions deploymentOptions;
    private VertxOptions options;
    private Consumer<Vertx> runner;
    private String verticleID;

    private Vertx vertx;

    public Vertx getVertx() {
        return vertx;
    }

    public VertxInitializer(NinjaProperties ninjaProperties) {
        options = new VertxOptions()
                .setClustered(ninjaProperties.getBooleanWithDefault(VERTX_IS_CLUSTERED, false));

        if (ninjaProperties.getBooleanWithDefault(VERTX_IS_METRICS_ENABLED, false)) {
            options.setMetricsOptions(new MetricsOptions().setEnabled(true));
        }

        deploymentOptions = new DeploymentOptions();
        if (ninjaProperties.getBooleanWithDefault(VERTX_IS_WORKER, true)) {
            deploymentOptions.setWorker(true).setWorkerPoolName("ninja-vertx").setInstances(
                    ninjaProperties.getIntegerWithDefault("vertx.workerPoolSize", DEFAULT_WORKER_POOL_SIZE));
        }
        if (options.isClustered()) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            Vertx.clusteredVertx(options, res -> {
                if (res.succeeded()) {
                    vertx = res.result();
                    countDownLatch.countDown();
                } else {
                    log.error(res.cause().getMessage(), res.cause());
                }
            });
            await(countDownLatch);
        } else {
            vertx = Vertx.vertx(options);
        }
    }

    private static VertxInitializer vertxInitializer;
    public synchronized static VertxInitializer getInstance(NinjaProperties ninjaProperties) {
        if (vertxInitializer == null) {
            vertxInitializer = new VertxInitializer(ninjaProperties);
        }
        return vertxInitializer;
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

        Handler<AsyncResult<String>> handler = stringAsyncResult -> {
            countDownLatch.countDown();
        };

        verticleID = GuiceVerticleFactory.PREFIX + ":" + NinjaVerticle.class.getName();
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
        runner.accept(vertx);
        await(countDownLatch);
    }

    @Dispose(order = 5)
    public void stop() {
        vertx.close();
    }


}

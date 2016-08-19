package com.jiabangou.ninja.vertx.standalone;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.metrics.MetricsOptions;
import ninja.lifecycle.Dispose;
import ninja.lifecycle.Start;
import ninja.utils.OverlayedNinjaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static void setNinjaVertx(NinjaVertx ninjaVertx) {
        VertxInitializer.ninjaVertx = ninjaVertx;
    }

    private static NinjaVertx ninjaVertx;


    private DeploymentOptions deploymentOptions;
    private Consumer<Vertx> runner;
    private String verticleID;
    private Vertx vertx;
    private OverlayedNinjaProperties overlayedNinjaProperties;

    @Inject
    public VertxInitializer() {
        this.overlayedNinjaProperties = new OverlayedNinjaProperties(ninjaVertx.getNinjaProperties());
    }

    @Start(order = 90)
    public void start() {
        NinjaVerticle.setPort(ninjaVertx.getPort());
        VertxOptions options = new VertxOptions()
                .setMetricsOptions(new MetricsOptions().setEnabled(true));
        deploymentOptions = new DeploymentOptions().setWorker(true)
                .setWorkerPoolName("ninja-vertx").setInstances(DEFAULT_WORKER_POOL_SIZE);
        verticleID = NinjaVerticle.class.getName();
        runner = vertex -> {
            try {
                if (deploymentOptions != null) {
                    vertex.deployVerticle(verticleID, deploymentOptions);
                } else {
                    vertex.deployVerticle(verticleID);
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
        } else {
            vertx = Vertx.vertx(options);
            runner.accept(vertx);
        }
    }

    @Dispose(order = 5)
    public void stop() {
        vertx.close();
    }


}

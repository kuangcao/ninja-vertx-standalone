package com.jiabangou.ninja.vertx.standalone;

import com.google.inject.Injector;
import io.vertx.core.VertxOptions;
import ninja.standalone.AbstractStandalone;

/**
 * Ninja Vertx
 * Created by freeway on 16/8/17.
 */
public class NinjaVertx extends AbstractStandalone<NinjaVertx> {

    private volatile NinjaVertxBootstrap ninjaVertxBootstrap;

    protected VertxOptions options;

    public NinjaVertx() {
        super("NinjaVertx");
    }

    public static void main(String[] args) {
        // create new instance and run it
        new NinjaVertx().run();
    }

    @Override
    protected void doConfigure() throws Exception {
        options = new VertxOptions();

        // fetch instance variable into method, so that we access the volatile
        // global variable only once - that's better performance wise.
        if (ninjaVertxBootstrap == null) {
            synchronized (this) {
                if (ninjaVertxBootstrap == null) {
                    VertxInitializer.setNinjaVertx(this);

                    ninjaVertxBootstrap = new NinjaVertxBootstrap(ninjaProperties);

                }
            }
        }
    }

    @Override
    protected void doStart() throws Exception {
        ninjaVertxBootstrap.boot();
    }

    @Override
    protected void doJoin() throws Exception {

    }

    @Override
    protected void doShutdown() {
        ninjaVertxBootstrap.shutdown();
    }

    @Override
    public Injector getInjector() {
        return ninjaVertxBootstrap.getInjector();
    }
}

package com.jiabangou.ninja.vertx.standalone;

import com.google.inject.Injector;
import ninja.standalone.AbstractStandalone;

/**
 * Ninja Vertx
 * Created by freeway on 16/8/17.
 */
public class NinjaVertx extends AbstractStandalone<NinjaVertx> {

    private volatile NinjaVertxBootstrap ninjaVertxBootstrap;

    public NinjaVertx() {
        super("NinjaVertx");
    }

    public static void main(String[] args) {
        // create new instance and run it
        new NinjaVertx().run();
    }

    @Override
    protected void doConfigure() throws Exception {

        // fetch instance variable into method, so that we access the volatile
        // global variable only once - that's better performance wise.
        if (ninjaVertxBootstrap == null) {
            synchronized (this) {
                if (ninjaVertxBootstrap == null) {
                    ninjaProperties.setProperty(KEY_NINJA_PORT, String.valueOf(this.port));
                    ninjaVertxBootstrap = new NinjaVertxBootstrap(ninjaProperties, getContextPath());
                }
            }
        }
    }

    @Override
    protected void doStart() throws Exception {
        try {
            this.ninjaVertxBootstrap.boot();
        } catch (Exception e) {
            throw tryToUnwrapInjectorException(e);
        }
    }

    @Override
    protected void doJoin() throws Exception {
        // vertx doesn't let us join it, so we'll instead wait ourselves
        synchronized(this) {
            this.wait();
        }
    }

    @Override
    protected void doShutdown() {
        if (ninjaVertxBootstrap != null) {
            ninjaVertxBootstrap.shutdown();
            ninjaVertxBootstrap = null;
        }
    }

    @Override
    public Injector getInjector() {
        checkStarted();
        return ninjaVertxBootstrap.getInjector();
    }
}

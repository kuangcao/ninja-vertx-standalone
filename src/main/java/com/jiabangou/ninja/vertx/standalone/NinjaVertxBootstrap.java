package com.jiabangou.ninja.vertx.standalone;

import com.google.inject.AbstractModule;
import ninja.Bootstrap;
import ninja.Context;
import ninja.utils.NinjaPropertiesImpl;

/**
 * NinjaVertxBootstrap
 * Created by freeway on 16/8/17.
 */
public class NinjaVertxBootstrap extends Bootstrap {


    public NinjaVertxBootstrap(NinjaPropertiesImpl ninjaProperties) {
        super(ninjaProperties);
    }

    @Override
    protected void configure() throws Exception {
        super.configure();
        NinjaVerticle.setBootstrap(this);

        // Context for servlet requests
        addModule(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Context.class).to(NinjaVertxServletContext.class);
                bind(VertxInitializer.class).asEagerSingleton();
            }
        });
    }
}

package com.jiabangou.ninja.vertx.standalone;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.jiabangou.ninja.vertx.standalone.guice.GuiceVerticleFactory;
import com.jiabangou.ninja.vertx.standalone.utils.PackageScan;
import ninja.Bootstrap;
import ninja.Context;
import ninja.utils.NinjaPropertiesImpl;

import java.util.List;

/**
 * NinjaVertxBootstrap
 * Created by freeway on 16/8/17.
 */
public class NinjaVertxBootstrap extends Bootstrap {

    public static final String CONF_CUNSUMER_ROUTES = "vertx.routes";

    private String contextPath;

    private final NinjaVertxBootstrap bootstrap;

    public String getContextPath() {
        return contextPath;
    }

    public NinjaVertxBootstrap(NinjaPropertiesImpl ninjaProperties, String contextPath) {
        super(ninjaProperties);
        this.contextPath = contextPath;
        this.bootstrap = this;
        GuiceVerticleFactory.setBootstrap(this);
    }


    protected void bindCunsumerRoutes() throws Exception {

        final List<Class<?>> classes = PackageScan.getClassList(CONF_CUNSUMER_ROUTES, false);
        addModule(new AbstractModule() {
            @Override
            protected void configure() {
                Multibinder<VertxRoutes> multibinder = Multibinder.newSetBinder(binder(), VertxRoutes.class);
                classes.forEach(aClass ->
                    multibinder.addBinding().to((Class<VertxRoutes>) aClass).in(Singleton.class)
                );
            }
        });

    }



    @Override
    protected void configure() throws Exception {
        super.configure();
        bindCunsumerRoutes();
        // Context for servlet requests
        addModule(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Context.class).to(NinjaVertxServletContext.class);
                bind(VertxInitializer.class).asEagerSingleton();
                bind(NinjaVertxBootstrap.class).toInstance(bootstrap);
                bind(NinjaHandler.class).toProvider(NinjaHandler.NinjaHandlerProvider.class);
            }

        });
    }
}

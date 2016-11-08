package com.jiabangou.ninja.vertx.standalone;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.jiabangou.ninja.vertx.standalone.guice.GuiceVerticleFactory;
import com.jiabangou.ninja.vertx.standalone.utils.PackageScan;
import io.vertx.core.Vertx;
import ninja.Bootstrap;
import ninja.Context;
import ninja.utils.NinjaPropertiesImpl;

import java.util.List;

/**
 * NinjaVertxBootstrap
 * Created by freeway on 16/8/17.
 */
public class NinjaVertxBootstrap extends Bootstrap {

    public static final String CONF_VERTX_ROUTES = "conf.VertxRoutes";
    public static final String CONF_VERTX_HANDLERS_ROUTES = "handlers";
    private String contextPath;

    private VertxInitializer vertxInitializer;

    private final NinjaVertxBootstrap bootstrap;

    public String getContextPath() {
        return contextPath;
    }

    public NinjaVertxBootstrap(NinjaPropertiesImpl ninjaProperties, String contextPath) {
        super(ninjaProperties);
        this.vertxInitializer = VertxInitializer.getInstance(ninjaProperties);
        this.contextPath = contextPath;
        this.bootstrap = this;
        GuiceVerticleFactory.setBootstrap(this);
    }

    public List<Class<?>> resolveVertxHandlers() {
        return PackageScan.getClasses(
                resolveApplicationClassName(CONF_VERTX_HANDLERS_ROUTES), true);
    }

    protected void bindVertxRoutes() throws Exception {

        String applicationRoutesClassName
                = resolveApplicationClassName(CONF_VERTX_ROUTES);

        if (doesClassExist(applicationRoutesClassName)) {

            final Class<? extends ApplicationVertxRoutes> vertxRoutes =
                    (Class<? extends ApplicationVertxRoutes>) Class.forName(applicationRoutesClassName);

            addModule(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(Vertx.class).toInstance(vertxInitializer.getVertx());
                    resolveVertxHandlers().forEach(this::bind);
                    bind(ApplicationVertxRoutes.class).to(vertxRoutes).in(Singleton.class);
                }
            });

        }

    }


    @Override
    protected void configure() throws Exception {
        super.configure();
        // Context for servlet requests
        addModule(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Context.class).to(NinjaVertxServletContext.class);
                bind(VertxInitializer.class).toInstance(vertxInitializer);
                bind(NinjaVertxBootstrap.class).toInstance(bootstrap);
                bind(NinjaContextBuilder.class);
                bind(NinjaHandler.class);
            }
        });
        bindVertxRoutes();
    }
}

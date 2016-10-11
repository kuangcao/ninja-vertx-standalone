package com.jiabangou.ninja.vertx.standalone;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provides;
import ninja.bodyparser.BodyParserEngineManager;
import ninja.params.ParamParsers;
import ninja.servlet.NinjaServletContext;
import ninja.session.FlashScope;
import ninja.session.Session;
import ninja.utils.NinjaProperties;
import ninja.utils.ResultHandler;
import ninja.validation.Validation;

import java.io.IOException;
import java.io.Writer;

/**
 * Ninja context for servlet environments.
 *
 * When modifying functionality for this class please carefully consider adding
 * it to <code>AbstractContext</code> first.  For example, instead of relying on
 * <code>httpServletRequest.getHeader()</code> you could reuse the existing
 * <code>this.getHeader()</code> and be able to implement your feature entirely
 * in <code>AbstractContext</code>.
 */
public class NinjaVertxServletContext extends NinjaServletContext {

    @Inject
    public NinjaVertxServletContext(
            BodyParserEngineManager bodyParserEngineManager,
            FlashScope flashScope,
            NinjaProperties ninjaProperties,
            ResultHandler resultHandler,
            Session session,
            Validation validation,
            Injector injector,
            ParamParsers paramParsers) {

        super(bodyParserEngineManager,
                flashScope,
                ninjaProperties,
                resultHandler,
                session,
                validation,
                injector,
                paramParsers);
    }

    @Override
    public void cleanup() {
        super.cleanup();

        try {
            Writer writer = getHttpServletResponse().getWriter();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
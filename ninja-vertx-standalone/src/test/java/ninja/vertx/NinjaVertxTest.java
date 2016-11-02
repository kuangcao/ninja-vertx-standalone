/*
 * Copyright 2015 Joe Lauer, Fizzed, Inc.
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

import com.google.inject.CreationException;
import com.kuangcao.ninja.vertx.standalone.NinjaVertx;
import ninja.standalone.Standalone;
import ninja.standalone.StandaloneHelper;
import ninja.utils.NinjaConstant;
import ninja.utils.NinjaMode;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

import static ninja.vertx.NinjaOkHttp3Tester.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class NinjaVertxTest {
    static private final Logger log = LoggerFactory.getLogger(NinjaVertxTest.class);

    static int randomPort = StandaloneHelper.findAvailablePort(8081, 9000);

    @Test
    public void startAndShutdownWithDefaults() throws Exception {
        System.out.println(StandardCharsets.UTF_8.name());
        // absolute minimal working version of application.conf
        NinjaVertx standalone = new NinjaVertx()
                .externalConfigurationPath("conf/vertx.minimal.conf")
                .port(randomPort);

        try {
            assertThat(standalone.getPort(), is(randomPort));
            assertThat(standalone.getHost(), is(nullValue()));
            assertThat(standalone.getContextPath(), is(nullValue()));
            assertThat(standalone.getNinjaMode(), is(NinjaMode.prod));

            try {
                standalone.getNinjaProperties();
            } catch (IllegalStateException e) {
                assertThat(e.getMessage(), containsString("configure() not called"));
            }

            try {
                standalone.getInjector();
            } catch (IllegalStateException e) {
                assertThat(e.getMessage(), containsString("start() not called"));
            }

            standalone.start();

            // this is everything that should have happened

            assertThat(standalone.getInjector(), is(not(nullValue())));
            assertThat(standalone.getNinjaProperties(), is(not(nullValue())));
            assertThat(standalone.getContextPath(), is(""));
            assertThat(standalone.getNinjaProperties().get(NinjaConstant.serverName), is("http://localhost:" + randomPort));
        } finally {
            standalone.shutdown();
        }
    }

    @Test
    public void missingConfigurationThrowsExceptionByStandalone() throws Exception {
        // bad configuration file will throw exception before ninja bootstrap
        NinjaVertx standalone = new NinjaVertx()
                .externalConfigurationPath("conf/vertx.empty.conf")
                .port(randomPort);

        try {
            standalone.start();
            fail("start() should have thrown exception");
        } catch (Exception e) {
            assertThat(e.getMessage(), containsString("application.secret not set"));
        } finally {
            standalone.shutdown();
        }
    }

    @Test
    public void missingLanguageThrowsExceptionByBootstrap() throws Exception {
        // bad configuration file will throw exception during ninja bootstrap
        NinjaVertx standalone = new NinjaVertx()
                .externalConfigurationPath("conf/vertx.missinglang.conf")
                .port(randomPort);

        try {
            standalone.start();
            fail("start() should have thrown exception");
        } catch (CreationException e) {
            assertThat(e.getMessage(), containsString("not retrieve application languages from ninjaProperties"));
        } finally {
            standalone.shutdown();
        }
    }

    @Test
    public void configurationPropertyPriority() throws Exception {
        try {
            // verify priority of configuration settings
            // currentValue then systemProperty then configValue then defaultValue
            // we'll used just the "port" to determine if its working as expected
            NinjaVertx standalone;

            // defaultValue
            standalone = new NinjaVertx()
                    .externalConfigurationPath("conf/vertx.minimal.conf");

            standalone.configure();

            assertThat(standalone.getPort(), is(8080));

            // configValue over defaultValue
            standalone = new NinjaVertx()
                    .externalConfigurationPath("conf/vertx.priority.conf");

            standalone.configure();

            assertThat(standalone.getPort(), is(9));

            // systemProperty over configValue
            System.setProperty(Standalone.KEY_NINJA_PORT, "1");
            standalone = new NinjaVertx()
                    .externalConfigurationPath("conf/vertx.priority.conf");

            standalone.configure();

            assertThat(standalone.getPort(), is(1));

            // currentValue over systemProperty
            standalone = new NinjaVertx()
                    .externalConfigurationPath("conf/vertx.priority.conf")
                    .port(randomPort)
                    .configure();

            assertThat(standalone.getPort(), is(randomPort));
        } finally {
            System.clearProperty(Standalone.KEY_NINJA_PORT);
        }
    }

    @Test
    public void basicIndex() throws Exception {
        NinjaVertx standalone = new NinjaVertx()
                .externalConfigurationPath("conf/vertx.example.conf")
                .port(randomPort);

        OkHttpClient client = NinjaOkHttp3Tester.newHttpClientBuilderWithLogging().build();

        try {
            standalone.start();
            Request request
                    = requestBuilder(standalone, "/")
                    .build();

            Response response = executeRequest(client, request);

            assertThat(response.body().string(), containsString("Hello World"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            standalone.shutdown();
        }
    }

    @Test
    public void withContextPath() throws Exception {
        NinjaVertx standalone = new NinjaVertx()
                .externalConfigurationPath("conf/vertx.example.conf")
                .ninjaMode(NinjaMode.test)
                .port(randomPort)
                .contextPath("/sample");

        OkHttpClient client = NinjaOkHttp3Tester.newHttpClientBuilderWithLogging().build();

        try {
            standalone.start();
            Request request
                    = requestBuilder(standalone, "/test")
                    .build();

            Response response = executeRequest(client, request);

            assertThat(response.body().string(), is("This test worked"));

            // context.getRequestPath altered by contextPath as well

            request
                    = requestBuilder(standalone, "/request_path")
                    .build();

            response = executeRequest(client, request);

            assertThat(response.body().string(), is("/request_path"));

        } finally {
            standalone.shutdown();
        }
    }

    @Test
    public void ssl() throws Exception {
        NinjaVertx standalone = new NinjaVertx()
                .externalConfigurationPath("conf/vertx.example.conf")
                .ninjaMode(NinjaMode.dev)
                .port(-1)
                .sslPort(randomPort);

        // build special http client that trusts the dev cert
        OkHttpClient client = NinjaOkHttp3Tester
                .newHttpClientBuilderWithLogging()
                .sslSocketFactory(trustAnySSLSocketFactory())
                .hostnameVerifier(trustAnyHostnameVerifier())
                .build();

        try {
            standalone.start();

            Request request
                    = requestBuilder(standalone, "/scheme")
                    .build();

            Response response = executeRequest(client, request);

            assertThat(response.body().string(), is("https"));
        } finally {
            standalone.shutdown();
        }
    }
}

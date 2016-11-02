/*
 * Copyright 2016 jiabangou, Inc.
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

import com.kuangcao.ninja.vertx.standalone.NinjaVertx;
import ninja.standalone.StandaloneHelper;
import ninja.utils.NinjaMode;
import okhttp3.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ninja.vertx.NinjaOkHttp3Tester.executeRequest;
import static ninja.vertx.NinjaOkHttp3Tester.requestBuilder;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.junit.Assert.assertThat;

public class VertxIntegrationTest {
    static private final Logger log = LoggerFactory.getLogger(VertxIntegrationTest.class);
    
    static private NinjaVertx standalone;
    static private OkHttpClient client;
    
    @BeforeClass
    static public void beforeClass() throws Exception {
        int randomPort = StandaloneHelper.findAvailablePort(8081, 9000);
        
        standalone  = new NinjaVertx()
            .externalConfigurationPath("conf/vertx.example.conf")
            .ninjaMode(NinjaMode.test)
            .port(randomPort)
            .start();
        
        client = NinjaOkHttp3Tester.newHttpClientBuilderWithLogging().build();
    }
    
    @AfterClass
    static public void afterClass() {
        standalone.shutdown();
    }
    
    @Test
    public void basicGet() throws Exception {
        Request request
            = requestBuilder(standalone, "/")
                .build();
        
        Response response = executeRequest(client, request);
          
        assertThat(response.code(), is(200));
        assertThat(response.header("Content-Type"), equalToIgnoringCase("text/html; charset=utf-8"));
        assertThat(response.body().string(), containsString("Hello World"));
    }

    @Test
    public void notFound() throws Exception {
        Request request
            = requestBuilder(standalone, "/doesnotexist")
                .build();
        
        Response response = executeRequest(client, request);
        
        assertThat(response.code(), is(404));
        assertThat(response.header("Content-Type"), equalToIgnoringCase("text/html; charset=utf-8"));
        assertThat(response.body().string(), containsString("requested route cannot be found"));
    }
    
    @Test
    public void withQueryParameters() throws Exception {
        Request request
            = requestBuilder(standalone, "/parameters?a=joe&b=2")
                .build();
        
        Response response = executeRequest(client, request);
        
        assertThat(response.code(), is(200));
        assertThat(response.header("Content-Type"), equalToIgnoringCase("text/html; charset=utf-8"));
        assertThat(response.body().string(), is("a=joe, b=2"));
    }
    
    @Test
    public void withFormParameters() throws Exception {
    	Request request
            = requestBuilder(standalone, "/parameters")
                .post(new FormBody.Builder()
                    .add("a", "joe")
                    .add("b", "2")
                    .build())
                .build();
        
        Response response = executeRequest(client, request);
        
        assertThat(response.code(), is(200));
        assertThat(response.header("Content-Type"), equalToIgnoringCase("text/html; charset=utf-8"));
        assertThat(response.body().string(), is("a=joe, b=2"));
    }
    
    @Test
    public void withFormParametersUnicode() throws Exception {
        String unicodeString = "Joe has $ — Jens has €. ÄÖÜßäöü";
    	Request request
            = requestBuilder(standalone, "/parameters")
                .post(new FormBody.Builder()
                    .add("a", unicodeString)
                    .add("b", "2")
                    .build())
                .build();
        
        Response response = executeRequest(client, request);
        
        assertThat(response.code(), is(200));
        assertThat(response.header("Content-Type"), equalToIgnoringCase("text/html; charset=utf-8"));
        assertThat(response.body().string(), is("a=" + unicodeString + ", b=2"));
    }
    
    @Test
    public void withQueryAndFormParameters() throws Exception {
        Request request
            = requestBuilder(standalone, "/parameters?a=joe")
                .post(new FormBody.Builder()
                    .add("b", "2")
                    .build())
                .build();
        
        Response response = executeRequest(client, request);
        
        assertThat(response.code(), is(200));
        assertThat(response.header("Content-Type"), equalToIgnoringCase("text/html; charset=utf-8"));
        assertThat(response.body().string(), is("a=joe, b=2"));
    }
    
    @Test
    public void withBoundForm() throws Exception {
        Request request
            = requestBuilder(standalone, "/basic_form?s=s")
                .post(new FormBody.Builder()
                    .add("s", "2nd_value_for_s_not_bound")
                    .add("i", "1")
                    .add("l", "2")
                    .add("b", "true")
                    .build())
                .build();
        
        Response response = executeRequest(client, request);
        
        assertThat(response.code(), is(200));
        assertThat(response.header("Content-Type"), equalToIgnoringCase("text/html; charset=utf-8"));
        assertThat(response.body().string(), is("s=s, i=1, l=2, b=true"));
    }
    
    @Test
    public void fileUpload1() throws Exception {
    	// simulated file content
    	String fileContent = "Undertow Upload";
    	String fileName = "test.txt";
    	String contentType = "text/plain";
    	
        Request request
            = requestBuilder(standalone, "/upload1")
                .post(new MultipartBody.Builder().setType(MultipartBody.FORM)
                		 .addFormDataPart("theFile",  fileName,  RequestBody.create(MediaType.parse(contentType), fileContent.getBytes()))
                	      .build())
                .build();
        
        Response response = executeRequest(client, request);
        
        assertThat(response.code(), is(200));
        assertThat(response.header("Content-Type"), equalToIgnoringCase("text/html; charset=utf-8"));
        assertThat(response.body().string(), is("l=" + fileContent.length()));
    }
    
    @Test
    public void fileUpload2() throws Exception {
    	// simulated file content
    	String fileContent1 = "Undertow Upload 1";
    	String fileContent2 = "Undertow Upload 2";
    	String fileName1 = "test1.txt";
    	String fileName2 = "test2.txt";
    	String contentType = "text/plain";
    	
        Request request
            = requestBuilder(standalone, "/upload2")
                .post(new MultipartBody.Builder().setType(MultipartBody.FORM)
                		 .addFormDataPart("theFile1",  fileName1,  RequestBody.create(MediaType.parse(contentType), fileContent1.getBytes()))
                		 .addFormDataPart("theFile2",  fileName2,  RequestBody.create(MediaType.parse(contentType), fileContent2.getBytes()))
                	      .build())
                .build();
        
        Response response = executeRequest(client, request);
        
        assertThat(response.code(), is(200));
        assertThat(response.header("Content-Type"), equalToIgnoringCase("text/html; charset=utf-8"));
        assertThat(response.body().string(), is(""));
    }
    
    @Test
    public void scheme() throws Exception {
        Request request
            = requestBuilder(standalone, "/scheme")
                .build();
        
        Response response = executeRequest(client, request);
        
        assertThat(response.code(), is(200));
        assertThat(response.header("Content-Type"), equalToIgnoringCase("text/html; charset=utf-8"));
        assertThat(response.body().string(), is("http"));
    }
    
    @Test
    public void remoteAddr() throws Exception {
        Request request
            = requestBuilder(standalone, "/remote_addr")
                .build();
        
        Response response = executeRequest(client, request);
        
        assertThat(response.code(), is(200));
        assertThat(response.header("Content-Type"), equalToIgnoringCase("text/html; charset=utf-8"));
        assertThat(response.body().string(), is("127.0.0.1"));
    }
    
    @Test
    public void requestPath() throws Exception {
        Request request
            = requestBuilder(standalone, "/request_path")
                .build();
        
        Response response = executeRequest(client, request);
        
        assertThat(response.code(), is(200));
        assertThat(response.header("Content-Type"), equalToIgnoringCase("text/html; charset=utf-8"));
        assertThat(response.body().string(), is("/request_path"));
    }
    
    /**
    @Test
    public void requestPathWithEncoded() throws Exception {
        String page = Requester.to(standalone)
                .GET("/request_path%2Fr");

        assertThat(page, is("/request_path"));
    }
    */
    
    @Test
    public void paramParsersAsQueryParam() throws Exception {
        Request request
            = requestBuilder(standalone, "/param_parsers?enum=a")
                .build();
        
        Response response = executeRequest(client, request);
        
        assertThat(response.code(), is(200));
        assertThat(response.header("Content-Type"), equalToIgnoringCase("text/html; charset=utf-8"));
        assertThat(response.body().string(), is("A"));
    }
    
    @Test
    public void paramParsersAsPost() throws Exception {
        Request request
            = requestBuilder(standalone, "/param_parsers")
                .post(new FormBody.Builder()
                    .add("enum", "b")
                    .build()
                )
                .build();
        
        Response response = executeRequest(client, request);
        
        assertThat(response.code(), is(200));
        assertThat(response.header("Content-Type"), equalToIgnoringCase("text/html; charset=utf-8"));
        assertThat(response.body().string(), is("B"));
    }
    
}

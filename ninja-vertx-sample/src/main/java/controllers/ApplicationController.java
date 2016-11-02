/**
 * Copyright (C) 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import io.vertx.core.json.JsonObject;
import ninja.Result;
import ninja.Results;
import redis.clients.jedis.Jedis;

import java.text.DateFormat;
import java.time.Instant;
import java.util.Date;


@Singleton
public class ApplicationController {

    @Inject
    private Provider<Jedis> provider;

    public Result index() {

        return Results.html();

    }

    public Result chat() {

        return Results.html();

    }

    public static void main(String[] args) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("systemTime",1);
        System.out.println(jsonObject.toString());
    }
    public Result helloWorldJson() {
        String timestamp = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(Date.from(Instant.now()));
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("systemTime",timestamp);

        provider.get().publish("com.example:cmd:poke-server",jsonObject.toString());
        SimplePojo simplePojo = new SimplePojo();
        simplePojo.content = "Hello World! Hello Json!";

        return Results.json().render(simplePojo);

    }
    
    public static class SimplePojo {

        public String content;
        
    }
}

package com.jiabangou.ninja.vertx.standalone;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;
import ninja.Context;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * RoutingContext 转换成 Context
 * Created by freeway on 2016/11/8.
 */
public class NinjaContextBuilder {

    private final Provider<Context>  context;
    private final NinjaVertxBootstrap bootstrap;

    @Inject
    public NinjaContextBuilder(Provider<Context> context, NinjaVertxBootstrap bootstrap) {
        this.context = context;
        this.bootstrap = bootstrap;
    }

    public Map<String,String> getCookie(RoutingContext event,String prefix,String secret){
        Map<String,String> cookieMap = Maps.newHashMap();
        if(StringUtils.isNotBlank(prefix) && StringUtils.isNotBlank(secret)){
            Cookie cookie = event.getCookie(prefix+"__SESSION");
            if(null != cookie) {
                String params = new CookieEncryption(secret).decrypt(cookie.getValue());
                cookieMap = paramsToMap(params);
            }
        }
        return cookieMap;
    }

    public Context build(RoutingContext event) {
        VertxHttpServletRequest request = new VertxHttpServletRequest(event);
        request.setContextPath(bootstrap.getContextPath());
        HttpServletResponse response = new VertxHttpServletResponse(event);
        // We generate a Ninja compatible context element
        NinjaVertxServletContext ninjaVertxServletContext = (NinjaVertxServletContext) context.get();
        // And populate it
        ninjaVertxServletContext.init(null, request, response);
        return ninjaVertxServletContext;
    }

    private Map<String, String> paramsToMap(String params) {
        Map<String, String> map = new LinkedHashMap<>();
        if (!Strings.isNullOrEmpty(params)) {
            String[] array = params.split("&");
            for (String pair : array) {
                if ("=".equals(pair.trim())) {
                    continue;
                }
                String[] entity = pair.split("=");
                if (entity.length == 1) {
                    map.put(entity[0], null);
                } else {
                    map.put(entity[0], entity[1]);
                }
            }
        }
        return map;
    }

    private class CookieEncryption {
        public static final String ALGORITHM = "AES";
        private final Optional<SecretKeySpec> secretKeySpec;

        public CookieEncryption(String secret) {
            Optional secretKeySpec = Optional.absent();
            try {
                int exception = Cipher.getMaxAllowedKeyLength("AES");
                if(exception == 2147483647) {
                    exception = 256;
                }

                secretKeySpec = Optional.of(new SecretKeySpec(secret.getBytes(), 0, exception / 8, "AES"));
            } catch (Exception var5) {
                throw new RuntimeException(var5);
            }

            this.secretKeySpec = secretKeySpec;
        }

        public String encrypt(String data) {
            Objects.requireNonNull(data, "Data to be encrypted");
            if(!this.secretKeySpec.isPresent()) {
                return data;
            } else {
                try {
                    Cipher ex = Cipher.getInstance("AES");
                    ex.init(1, (Key)this.secretKeySpec.get());
                    byte[] encrypted = ex.doFinal(data.getBytes(StandardCharsets.UTF_8));
                    return Base64.encodeBase64URLSafeString(encrypted);
                } catch (InvalidKeyException var4) {
                    throw new RuntimeException(var4);
                } catch (GeneralSecurityException var5) {
                    throw new RuntimeException(var5);
                }
            }
        }

        public String decrypt(String value) {
            String data = value.substring(value.indexOf("-") + 1);
            Objects.requireNonNull(data, "Data to be decrypted");
            if(!this.secretKeySpec.isPresent()) {
                return data;
            } else {
                byte[] decoded = Base64.decodeBase64(data);

                try {
                    Cipher ex = Cipher.getInstance("AES");
                    ex.init(2, (Key)this.secretKeySpec.get());
                    byte[] decrypted = ex.doFinal(decoded);
                    return new String(decrypted, StandardCharsets.UTF_8);
                } catch (InvalidKeyException var5) {
                    throw new RuntimeException(var5);
                } catch (GeneralSecurityException var6) {
                    throw new RuntimeException(var6);
                }
            }
        }
    }

}

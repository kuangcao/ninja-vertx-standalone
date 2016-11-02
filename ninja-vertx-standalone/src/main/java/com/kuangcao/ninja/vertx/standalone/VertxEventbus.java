package com.kuangcao.ninja.vertx.standalone;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Provider;
import com.kuangcao.ninja.vertx.standalone.model.EventbusVo;
import com.kuangcao.ninja.vertx.standalone.model.Result;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by wangziqing on 16/10/31.
 */
public class VertxEventbus {
    private io.vertx.ext.web.Router router;
    private Vertx vertx;
    private Map<Class, Object> eventbusMap;
    private Provider<Jedis> provider;

    private IVertxError iVertxError;

    public VertxEventbus(io.vertx.ext.web.Router router, Vertx vertx, Map<Class, Object> eventbusMap, Provider<Jedis> provider,IVertxError iVertxError) {
        this.router = router;
        this.vertx = vertx;
        this.eventbusMap = eventbusMap;
        this.provider = provider;
        this.iVertxError = iVertxError;
    }

    public static VertxEventbus build(io.vertx.ext.web.Router router, Vertx vertx, Map<Class, Object> eventbusMap, Provider<Jedis> provider,IVertxError iVertxError) {
        return new VertxEventbus(router, vertx, eventbusMap, provider,iVertxError);
    }

    public VRouter route(String path) {
        return new VRouter(router, vertx, path);
    }

    public class VRouter {
        private io.vertx.ext.web.Router router;
        private Vertx vertx;
        private BridgeOptions opts;
        private String path;

        public VRouter(io.vertx.ext.web.Router router, Vertx vertx, String path) {
            this.router = router;
            this.vertx = vertx;
            opts = new BridgeOptions();
            this.path = path;
        }

        public void with(Class cls) {
            Object object = eventbusMap.get(cls);
            if (null == object) {
                throw new RuntimeException("无效的eventBus");
            }
            List<EventbusVo> eventbusVos = this.findMethod(object);
            if (null != eventbusVos && !eventbusVos.isEmpty()) {

                EventBus eb = vertx.eventBus();
                for (EventbusVo eventbus : eventbusVos) {
                    if (Strings.isNullOrEmpty(eventbus.getInBound())) {
                        continue;
                    }
                    opts.addInboundPermitted(new PermittedOptions().setAddressRegex(eventbus.getInBound()));
                    if (!Strings.isNullOrEmpty(eventbus.getOutBound())) {
                        opts.addOutboundPermitted(new PermittedOptions().setAddressRegex(eventbus.getOutBound()));
                    }

                    eb.consumer(eventbus.getInBound()).handler(msg ->
                            provider.get().publish(eventbus.getInBound(), String.valueOf(msg.body()))
                    );
                    new Thread(() -> provider.get().subscribe(new JedisPubSub() {
                        @Override
                        public void onMessage(String channel, String message) {
                            Object returnObj = null;
                            try {
                                if (eventbus.getParameterTypes().length > 0) {
                                    returnObj = eventbus.getMethod().invoke(object, message);
                                } else {
                                    returnObj = eventbus.getMethod().invoke(object);
                                }
                                if (!Strings.isNullOrEmpty(eventbus.getOutBound())) {
                                    //     JsonObject jsonObject = returnObj == null? new JsonObject():toVertxJson((JSONObject)returnObj);
//                                    if(null != returnObj){
//                                    }
                                    eb.publish(eventbus.getOutBound(), toVertxJson(returnObj));
                                }
                            } catch (Exception e) {
                                if(null != iVertxError){
                                    eb.publish(eventbus.getOutBound(), new JsonObject(iVertxError.errorExcute(e)));
                                }
                                e.printStackTrace();
                            }
                        }
                    }, eventbus.getInBound())).start();
                }

                SockJSHandlerOptions options = new SockJSHandlerOptions().setHeartbeatInterval(2000);
                SockJSHandler ebHandler = SockJSHandler.create(vertx, options).bridge(opts);
                router.route(path).handler(ebHandler);
            }

        }

        private JsonObject toVertxJson(Object object) {
            JsonObject vertxJson = new JsonObject();
            if(null == object){
                return vertxJson;
            }
            Result result = (Result)object;
            JSONObject jsonObject  = JSONObject.parseObject(JSONObject.toJSONString(result));

            Set<Map.Entry<String, Object>> set = jsonObject.entrySet();
            set.forEach(s ->
                    vertxJson.put(s.getKey(), s.getValue())
            );
            return vertxJson;
        }

        private List<EventbusVo> findMethod(Object object) {
            try {
                List<EventbusVo> eventbusVoList = Lists.newArrayList();
                Method[] methods = object.getClass().getDeclaredMethods();
                for (Method method : methods) {
                    Annotation annotationS = method.getAnnotation(Permitted.class);
                    if (null != annotationS) {
                        EventbusVo eventbusVo = new EventbusVo();
                        eventbusVo.setMethod(method);
                        eventbusVo.setParameterTypes(method.getParameterTypes());

                        Method inBound = annotationS.getClass().getDeclaredMethod("inBound");
                        Object inBoundBbj = null;
                        if (null != (inBoundBbj = inBound.invoke(annotationS))) {
                            eventbusVo.setInBound(inBoundBbj.toString());
                        }

                        Method outBound = annotationS.getClass().getDeclaredMethod("outBound");
                        Object outBoundObj = null;
                        if (null != (outBoundObj = outBound.invoke(annotationS))) {
                            eventbusVo.setOutBound(outBoundObj.toString());
                        }
                        eventbusVoList.add(eventbusVo);
                    }
                }
                return eventbusVoList;
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }

    }


}

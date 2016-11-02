package com.kuangcao.ninja.vertx.standalone.model;

import java.lang.reflect.Method;

/**
 * Created by wangziqing on 16/11/1.
 */
public class EventbusVo {

    private String inBound;
    private String outBound;

    private Method method;

    private Class<?>[] parameterTypes;

    public String getInBound() {
        return inBound;
    }

    public void setInBound(String inBound) {
        this.inBound = inBound;
    }

    public String getOutBound() {
        return outBound;
    }

    public void setOutBound(String outBound) {
        this.outBound = outBound;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }
}

package com.github.frankfarrell.proxytransformer.context.request;

import java.util.Map;

public class RequestHeadersContextHolder {

    private static final ThreadLocal<Map<String,String>> contextHolder = new InheritableThreadLocal<>();

    public static void setContext(Map<String,String> headers) {
        contextHolder.set(headers);
    }

    public static Map<String,String> getContext() {
        return contextHolder.get();
    }

    public static void clearContext() {
        contextHolder.remove();
    }



}

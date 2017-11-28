package com.github.frankfarrell.proxytransformer.context.request;

import java.util.Map;

public class RequestQueryParamsContextHolder {

    private static final ThreadLocal<Map<String,String>> contextHolder = new InheritableThreadLocal<>();

    public static void setContext(Map<String,String> queryParams) {
        contextHolder.set(queryParams);
    }

    public static Map<String,String> getContext() {
        return contextHolder.get();
    }

    public static void clearContext() {
        contextHolder.remove();
    }
}

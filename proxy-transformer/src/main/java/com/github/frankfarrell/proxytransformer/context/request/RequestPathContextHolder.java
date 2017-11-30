package com.github.frankfarrell.proxytransformer.context.request;


public class RequestPathContextHolder {

    private static final ThreadLocal<RequestPath> contextHolder = new InheritableThreadLocal<>();

    public static void setContext(RequestPath path) {
        contextHolder.set(path);
    }

    public static RequestPath getContext() {
        return contextHolder.get();
    }

    public static void clearContext() {
        contextHolder.remove();
    }
}

package com.github.frankfarrell.proxytransformer.context.request;

public class RequestMethodContextHolder {

    private static final ThreadLocal<String> contextHolder = new InheritableThreadLocal<>();

    public static void setContext(final String method) {
        contextHolder.set(method);
    }

    public static String getContext() {
        return contextHolder.get();
    }

    public static void clearContext() {
        contextHolder.remove();
    }
}

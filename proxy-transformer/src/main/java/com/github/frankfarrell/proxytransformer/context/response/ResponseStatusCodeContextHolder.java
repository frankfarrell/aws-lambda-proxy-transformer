package com.github.frankfarrell.proxytransformer.context.response;

public class ResponseStatusCodeContextHolder {

    private static final ThreadLocal<String> contextHolder = new InheritableThreadLocal<>();

    public static void setContext(String status) {
        contextHolder.set(status);
    }

    public static String getContext() {
        return contextHolder.get();
    }

    public static void clearContext() {
        contextHolder.remove();
    }
}

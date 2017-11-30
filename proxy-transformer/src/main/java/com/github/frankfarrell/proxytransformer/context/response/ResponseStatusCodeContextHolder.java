package com.github.frankfarrell.proxytransformer.context.response;

public class ResponseStatusCodeContextHolder {

    private static final ThreadLocal<Integer> contextHolder = new InheritableThreadLocal<>();

    public static void setContext(Integer status) {
        contextHolder.set(status);
    }

    public static Integer getContext() {
        return contextHolder.get();
    }

    public static void clearContext() {
        contextHolder.remove();
    }
}

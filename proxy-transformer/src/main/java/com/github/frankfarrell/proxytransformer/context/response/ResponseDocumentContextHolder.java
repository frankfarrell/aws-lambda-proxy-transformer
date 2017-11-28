package com.github.frankfarrell.proxytransformer.context.response;

public class ResponseDocumentContextHolder {

    private static final ThreadLocal<Object> contextHolder = new InheritableThreadLocal<>();

    public static void setContext(Object document) {
        contextHolder.set(document);
    }

    public static Object getContext() {
        return contextHolder.get();
    }

    public static void clearContext() {
        contextHolder.remove();
    }


}

package com.github.frankfarrell.proxytransformer.context.request;

public class RequestDocumentContextHolder {

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

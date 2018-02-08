package com.github.frankfarrell.proxytransformer.context.response;

public class ResponseDocumentContextHolder {

    private static final ThreadLocal<String> contextHolder = new InheritableThreadLocal<>();

    public static void setContext(String document) {
        contextHolder.set(document);
    }

    public static String getContext() {
        return contextHolder.get();
    }

    public static void clearContext() {
        contextHolder.remove();
    }


}

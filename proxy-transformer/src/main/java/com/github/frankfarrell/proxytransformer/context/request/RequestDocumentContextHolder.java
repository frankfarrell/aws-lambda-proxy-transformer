package com.github.frankfarrell.proxytransformer.context.request;

import java.util.Optional;

public class RequestDocumentContextHolder {

    private static final ThreadLocal<Optional<Object>> contextHolder = new InheritableThreadLocal<>();

    public static void setContext(Object document) {
        contextHolder.set(Optional.ofNullable(document));
    }

    public static Optional<Object> getContext() {
        return contextHolder.get();
    }

    public static void clearContext() {
        contextHolder.remove();
    }



}

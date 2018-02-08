package com.github.frankfarrell.proxytransformer.context.request;

import java.util.Optional;

public class RequestDocumentContextHolder {

    private static final ThreadLocal<Optional<String>> contextHolder = new InheritableThreadLocal<>();

    public static void setContext(String document) {
        contextHolder.set(Optional.ofNullable(document));
    }

    public static Optional<String> getContext() {
        return contextHolder.get();
    }

    public static void clearContext() {
        contextHolder.remove();
    }



}

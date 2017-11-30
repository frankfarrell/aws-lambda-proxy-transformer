package com.github.frankfarrell.proxytransformer.context.request;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class RequestQueryParamsContextHolder {

    private static final ThreadLocal<Optional<Map<String,String>>> contextHolder = new InheritableThreadLocal<>();

    public static void setContext(Map<String,String> queryParams) {
        contextHolder.set(Optional.ofNullable(queryParams));
    }

    public static Map<String,String> getContext() {
        return contextHolder.get().orElse(Collections.emptyMap());
    }

    public static void clearContext() {
        contextHolder.remove();
    }
}

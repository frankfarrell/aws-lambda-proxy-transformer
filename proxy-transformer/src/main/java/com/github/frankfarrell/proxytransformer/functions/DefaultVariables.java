package com.github.frankfarrell.proxytransformer.functions;

import com.github.frankfarrell.proxytransformer.context.request.RequestMethodContextHolder;
import com.github.frankfarrell.proxytransformer.context.request.RequestPathContextHolder;
import com.github.frankfarrell.proxytransformer.context.response.ResponseStatusCodeContextHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class DefaultVariables {

    public static final Map<String, Supplier<Object>> getDefaultSupplierFunctions(){
        final Map<String, Supplier<Object>> functionMap = new HashMap<>();
        functionMap.put("requestPath", requestPath());
        functionMap.put("requestMethod", requestMethod());
        functionMap.put("responseStatusCode", responseStatusCode());
        return functionMap;
    }

    public static Supplier<Object> requestPath(){
        return () -> RequestPathContextHolder.getContext().path;
    }

    public static Supplier<Object> requestMethod(){
        return RequestMethodContextHolder::getContext;
    }

    public static Supplier<Object> responseStatusCode(){
        return ResponseStatusCodeContextHolder::getContext;
    }
}

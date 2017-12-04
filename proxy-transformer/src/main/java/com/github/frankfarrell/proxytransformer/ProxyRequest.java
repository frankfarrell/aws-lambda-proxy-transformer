package com.github.frankfarrell.proxytransformer;

import com.github.frankfarrell.proxytransformer.config.HttpMethod;

import java.util.Map;
import java.util.Optional;

public class ProxyRequest {

    final HttpMethod currentHttpMethod;
    final String currentPath;
    final Map<String, String> queryParams;
    final Map<String, String> headers;
    final Optional<String> body;

    public ProxyRequest(final HttpMethod currentHttpMethod,
                        final String currentPath,
                        final Map<String, String> queryParams,
                        final Map<String, String> headers,
                        final String body) {
        this.currentHttpMethod = currentHttpMethod;
        this.currentPath = currentPath;
        this.queryParams = queryParams;
        this.headers = headers;
        this.body = Optional.ofNullable(body);
    }
}

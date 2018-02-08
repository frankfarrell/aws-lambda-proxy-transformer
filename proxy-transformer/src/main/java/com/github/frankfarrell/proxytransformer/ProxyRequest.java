package com.github.frankfarrell.proxytransformer;

import com.github.frankfarrell.proxytransformer.config.HttpMethod;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class ProxyRequest {

    public final HttpMethod currentHttpMethod;
    public final String currentPath;
    public final Map<String, String> queryParams;
    public final Map<String, String> headers;
    public final Optional<String> body;

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

    private ProxyRequest(Builder builder) {
        currentHttpMethod = builder.currentHttpMethod;
        currentPath = builder.currentPath;
        queryParams = builder.queryParams;
        headers = builder.headers;
        body = builder.body;
    }

    public static Builder builder(final HttpMethod currentHttpMethod,
                                  final String currentPath) {
        return new Builder(currentHttpMethod, currentPath);
    }

    public static Builder builder(ProxyRequest copy) {
        Builder builder = new Builder(copy.currentHttpMethod, copy.currentPath);
        builder.queryParams = copy.queryParams;
        builder.headers = copy.headers;
        builder.body = copy.body;
        return builder;
    }

    public static final class Builder {
        private final HttpMethod currentHttpMethod;
        private final String currentPath;
        private Map<String, String> queryParams = Collections.emptyMap();
        private Map<String, String> headers = Collections.emptyMap();
        private Optional<String> body = Optional.empty();

        private Builder(final HttpMethod currentHttpMethod,
                        final String currentPath) {
            this.currentHttpMethod = currentHttpMethod;
            this.currentPath = currentPath;
        }

        public Builder withQueryParams(Map<String, String> queryParams) {
            this.queryParams = queryParams;
            return this;
        }

        public Builder withHeaders(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder withBody(Optional<String> body) {
            this.body = body;
            return this;
        }

        public ProxyRequest build() {
            return new ProxyRequest(this);
        }
    }
}

package com.github.frankfarrell.proxytransformer;

import java.util.Map;

public class ProxyResponse {

    public final String body;
    public final Map<String, String> headers;
    public final Integer statusCode;

    public ProxyResponse(final String body,
                         final Map<String, String> headers,
                         final Integer statusCode) {
        this.body = body;
        this.headers = headers;
        this.statusCode = statusCode;
    }
}

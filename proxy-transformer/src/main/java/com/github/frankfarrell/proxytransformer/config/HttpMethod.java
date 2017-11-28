package com.github.frankfarrell.proxytransformer.config;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Created by ffarrell on 24/11/2017.
 */
public enum HttpMethod {

    GET, POST, DELETE, PUT, PATCH, OPTIONS, HEAD;

    @JsonCreator
    public static HttpMethod forValue(String v) {
        return HttpMethod.valueOf(v);
    }

}

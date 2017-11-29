package com.github.frankfarrell.proxytransformer.config;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

public enum HttpMethod {

    GET("GET"), 
    POST("POST"), 
    DELETE("DELETE"), 
    PUT("PUT"), 
    PATCH("PATCH"), 
    OPTIONS("OPTIONS"), 
    HEAD("HEAD");

    public final String value;

    HttpMethod(String value) {
        this.value = value;
    }


    @JsonCreator
    public static HttpMethod forValue(String v) {
        return Arrays.stream(HttpMethod.values()).filter(val->val.value.equalsIgnoreCase(v)).findFirst().orElseThrow(()-> new RuntimeException("No value"));}

}

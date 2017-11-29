package com.github.frankfarrell.proxytransformer.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Optional;

public class ProxyConfiguration {

    public final HttpMethod inputMethod;
    public final String inputPathPattern;
    public final HttpMethod destinationMethod;
    public final String destinationPath; //This can include regexes groups that were matching in inputPathPattern, $1, $2 etc

    //REQUEST
    //If any of the following aren't present headers/body are returned to client as-is
    //Headers to send
    public final Optional<Map<String, String>> headersToSend; // Can use functions to match input headers

    //Query Params to send
    public final Optional<Map<String, String>> queryParamsToSend; //Can use functions to match input query params

    //Body to send
    public final Optional<Map<String, Object>> bodyToSend; //Can use functions to match input json

    //TODO These can be in plaintext or can use a function to query them from somewhere else
    //Functions include querying from env varible, or you could write our own custom function to query from s3 or somewhere
    public final Optional<String> requestUsername;
    public final Optional<String> requestPassword;

    //RESPONSE
    //Headers returned to client
    public final Optional<Map<String, String>> responseHeaders;

    //Headers returned to client
    public final Optional<String> responseStatusCode;

    //Body returned to client
    public final Optional<Map<String, Object>> responseBody;

    @JsonCreator
    public ProxyConfiguration(@JsonProperty("inputMethod") final HttpMethod inputMethod,
                              @JsonProperty("inputPathPattern") final String inputPathPattern,
                              @JsonProperty("destinationMethod") final HttpMethod destinationMethod,
                              @JsonProperty("destinationPath") final String destinationPath,
                              @JsonProperty("headersToSend") final Map<String, String> headersToSend,
                              @JsonProperty("queryParamsToSend") final Map<String, String> queryParamsToSend,
                              @JsonProperty("bodyToSend") final Map<String, Object> bodyToSend,
                              @JsonProperty("requestUsername") final String requestUsername,
                              @JsonProperty("requestPassword") final String requestPassword,
                              @JsonProperty("responseHeaders")final Map<String, String> responseHeaders,
                              @JsonProperty("responseStatusCode") final String responseStatusCode,
                              @JsonProperty("responseBody") final Map<String, Object> responseBody) {
        this.inputMethod = inputMethod;
        this.inputPathPattern = inputPathPattern;
        this.destinationMethod = destinationMethod;
        this.destinationPath = destinationPath;
        this.headersToSend = Optional.ofNullable(headersToSend);
        this.queryParamsToSend = Optional.ofNullable(queryParamsToSend);
        this.bodyToSend = Optional.ofNullable(bodyToSend);

        //TODO Check on the following, if one is present so must the other,
        //Log a warning and continue?
        this.requestUsername = Optional.ofNullable(requestUsername);
        this.requestPassword = Optional.ofNullable(requestPassword);
        this.responseHeaders = Optional.ofNullable(responseHeaders);
        this.responseStatusCode = Optional.ofNullable(responseStatusCode);
        this.responseBody = Optional.ofNullable(responseBody);
    }
}

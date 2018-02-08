package com.github.frankfarrell.proxytransformer.example.awslambda;


import com.amazonaws.serverless.proxy.internal.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.internal.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.frankfarrell.proxytransformer.ProxyBaseHandler;
import com.github.frankfarrell.proxytransformer.ProxyRequest;
import com.github.frankfarrell.proxytransformer.ProxyResponse;
import com.github.frankfarrell.proxytransformer.config.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LambdaProxyHandler implements RequestStreamHandler {

    private static final Logger log = LoggerFactory.getLogger(LambdaProxyHandler.class);

    private final ObjectMapper objectMapper;

    private final ProxyBaseHandler proxyBaseHandler;

    public LambdaProxyHandler() throws IOException {
        this(ProxyBaseHandler.getDefaultObjectMapper(),
                new ProxyBaseHandler(new File("src/main/resources/config.json")));
    }

    public LambdaProxyHandler(final ObjectMapper objectMapper,
                              final ProxyBaseHandler proxyBaseHandler) {
        this.objectMapper = objectMapper;
        this.proxyBaseHandler = proxyBaseHandler;
    }

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {

        final AwsProxyRequest request = this.objectMapper.readValue(input, AwsProxyRequest.class);

        final ProxyRequest proxyRequest =
                new ProxyRequest(HttpMethod.forValue(request.getHttpMethod()),
                        request.getPath(),
                        request.getQueryStringParameters(),
                        request.getHeaders(),
                        request.getBody());

        final ProxyResponse proxyResponse = proxyBaseHandler.handleRequest(proxyRequest);

        final AwsProxyResponse resp = new AwsProxyResponse();
        resp.setBody(proxyResponse.body);
        resp.setHeaders(proxyResponse.headers);
        resp.setStatusCode(proxyResponse.statusCode);
        this.objectMapper.writeValue(output, resp);
        output.close();
    }
}

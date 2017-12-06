package com.github.frankfarrell.proxytransformer.example.awslambda;


import com.amazonaws.serverless.proxy.internal.model.AwsProxyRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.frankfarrell.proxytransformer.ProxyBaseHandler;
import com.github.frankfarrell.proxytransformer.ProxyResponse;
import com.sun.deploy.net.proxy.ProxyHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;


//import static com.jayway.jsonpath.matchers.JsonPathMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class LambdaProxyBaseHandlerTest {

    LambdaProxyHandler lambdaProxyHandlerUnderTest;

    @Mock
    InputStream mockInputStream;

    @Mock
    ProxyBaseHandler mockProxyBaseHandler;

    @Mock
    Context mockConext;
    ObjectMapper spiedObjectMapper;

    AwsProxyRequest mockAwsProxyRequest;

    ByteArrayOutputStream outputStream;

    @Before
    public void setip() throws IOException {
        outputStream = new ByteArrayOutputStream();
        MockitoAnnotations.initMocks(this);

        spiedObjectMapper = Mockito.spy(ProxyBaseHandler.getDefaultObjectMapper());

        lambdaProxyHandlerUnderTest = new LambdaProxyHandler(spiedObjectMapper, mockProxyBaseHandler);
        Mockito.doReturn(mockAwsProxyRequest).when(spiedObjectMapper).readValue(Matchers.any(InputStream.class), Matchers.eq(AwsProxyRequest.class));

    }


    @Test
    public void itWorks() throws IOException {

        mockAwsProxyRequest = new AwsProxyRequest();
        mockAwsProxyRequest.setHttpMethod("GET");
        mockAwsProxyRequest.setPath("/test/94");

        Mockito.doReturn(mockAwsProxyRequest).when(spiedObjectMapper).readValue(Matchers.any(InputStream.class), Matchers.eq(AwsProxyRequest.class));

        when(mockProxyBaseHandler.handleRequest(any()))
                .thenReturn(new ProxyResponse(
                        "{\"test\":true}",
                        Collections.singletonMap("header", "value"),
                        201));

        lambdaProxyHandlerUnderTest.handleRequest(mockInputStream, outputStream, mockConext);

        String jsonString = getJsonStringResponse();
        //TODO
        //assertThat(jsonString, isJson());
        //assertThat(jsonString, hasJsonPath("$.statusCode", equalTo(500)));

    }

    private String getJsonStringResponse() {
        byte[] byteArray = outputStream.toByteArray();
        return new String(byteArray);
    }

}

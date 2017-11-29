package com.github.frankfarrell.proxytransformer;


import com.amazonaws.serverless.proxy.internal.model.AwsProxyRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class ProxyBaseHandlerTest {

    ProxyBaseHandler proxyBaseHandlerUnderTest;

    @Mock
    InputStream mockInputStream;

    @Mock
    Context mockConext;
    ObjectMapper spiedObjectMapper;

    AwsProxyRequest mockAwsProxyRequest;

    ByteArrayOutputStream outputStream;

    @Before
    public void setip() throws IOException {
        outputStream = new ByteArrayOutputStream();
        MockitoAnnotations.initMocks(this);

        spiedObjectMapper = spy(ProxyBaseHandler.getDefaultObjectMapper());

        proxyBaseHandlerUnderTest = new ProxyBaseHandler(spiedObjectMapper);
        doReturn(mockAwsProxyRequest).when(spiedObjectMapper).readValue(any(InputStream.class), eq(AwsProxyRequest.class));

    }


    @Test
    public void itWorks() throws IOException {

        mockAwsProxyRequest = new AwsProxyRequest();
        mockAwsProxyRequest.setHttpMethod("GET");
        mockAwsProxyRequest.setPath("/test/94");

        doReturn(mockAwsProxyRequest).when(spiedObjectMapper).readValue(any(InputStream.class), eq(AwsProxyRequest.class));

        proxyBaseHandlerUnderTest.handleRequest(mockInputStream, outputStream, mockConext);
    }

}

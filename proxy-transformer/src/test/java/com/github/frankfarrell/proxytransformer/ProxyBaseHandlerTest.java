package com.github.frankfarrell.proxytransformer;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.frankfarrell.proxytransformer.config.HttpMethod;
import com.github.frankfarrell.proxytransformer.config.ProxyConfiguration;
import com.mashape.unirest.http.Unirest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.Function;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class ProxyBaseHandlerTest {

    ProxyBaseHandler proxyBaseHandlerUnderTest;

    @Mock
    Unirest mockUnirest;

    @Mock
    File mockConfigFile;

    ObjectMapper spiedObjectMapper;

    ProxyConfiguration mockProxyConfiguration;

    @Before
    public void setup() throws IOException {

        mockProxyConfiguration = new ProxyConfiguration(HttpMethod.GET,
                "hello/(\\d*)",
                HttpMethod.PUT,
                "test");

        MockitoAnnotations.initMocks(this);
        spiedObjectMapper = Mockito.spy(ProxyBaseHandler.getDefaultObjectMapper());

        doReturn(Collections.singletonList(mockProxyConfiguration)).when(spiedObjectMapper).readValue(any(File.class), any(TypeReference.class));

        proxyBaseHandlerUnderTest =
                new ProxyBaseHandler(new HashMap<>(),
                        new HashMap<>(),
                        new HashMap<>(),
                        spiedObjectMapper,
                        mockConfigFile,
                        mockUnirest);

            }


    @Test
    public void itShouldParseThePathCorrectly(){
        proxyBaseHandlerUnderTest.getMatchedPathGroups("hello/123", "hello/(\\d*)");

        //TODO tests
    }

}

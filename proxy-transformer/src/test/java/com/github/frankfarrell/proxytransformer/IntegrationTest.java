package com.github.frankfarrell.proxytransformer;

import com.github.frankfarrell.proxytransformer.config.HttpMethod;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by ffarrell on 08/02/2018.
 */
public class IntegrationTest {

    ProxyBaseHandler proxyBaseHandlerUnderTest;

    @Before
    public void setup() throws IOException {
        proxyBaseHandlerUnderTest = new ProxyBaseHandler(new File("src/test/resources/sample-config.json"));
    }

    @Test
    public void itCallsTheAllDogsApi() throws IOException {
        ProxyResponse response = proxyBaseHandlerUnderTest.handleRequest(ProxyRequest.builder(HttpMethod.GET, "dogs").build());
        assertThat(response.body).isNotEmpty();
    }

    @Test
    public void itCallsTheAllBreedsEndpoint() throws IOException {
        ProxyResponse response = proxyBaseHandlerUnderTest.handleRequest(ProxyRequest.builder(HttpMethod.GET, "dogs/hound").build());
        assertThat(response.body).isNotEmpty();
    }

}

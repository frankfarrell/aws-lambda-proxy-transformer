package com.github.frankfarrell.proxytransformer.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.frankfarrell.proxytransformer.ProxyBaseHandler;
import com.github.frankfarrell.proxytransformer.context.response.ResponseStatusCodeContextHolder;
import com.github.frankfarrell.proxytransformer.functions.DefaultBiFunctions;
import com.github.frankfarrell.proxytransformer.parser.ExpressionParser;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.revinate.assertj.json.JsonPathAssert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class ResponseTransformerTest {

    ResponseTransformer responseTransformerUnderTest;

    @Mock
    ExpressionParser mockExpressionParser;

    @Before
    public void init(){
        MockitoAnnotations.initMocks(this);
        responseTransformerUnderTest = new ResponseTransformer(ProxyBaseHandler.getDefaultObjectMapper(), mockExpressionParser);

        doAnswer((z) -> (Function<Void, Object>) aVoid -> "CONCAT").when(mockExpressionParser).parseAndBuildFunction(startsWith("concat"));
        doAnswer((z)  -> (Function<Void, Object>) aVoid -> "UPPER").when(mockExpressionParser).parseAndBuildFunction(startsWith("toUpper"));
    }

    @Test
    public void itAppliesTheFunctionIfAFunctionIsSpecified(){

        when(mockExpressionParser.parseAndBuildFunction(any())).thenReturn((z) -> DefaultBiFunctions.concat().apply("20", "1"));

        Optional<String> input = Optional.of("join('20', '1'");

        assertThat(responseTransformerUnderTest.transformResponseStatusCode(input)).isEqualTo(201);
    }

    @Test
    public void itUsesTheResponseStatusCodeIfInputIsEmpty(){

        ResponseStatusCodeContextHolder.setContext("500");
        Optional<String> input = Optional.empty();

        assertThat(responseTransformerUnderTest.transformResponseStatusCode(input)).isEqualTo(500);
    }

    @Test
    public void itAppliesTheFunctionForEachHeaderIfSpecified(){

        Map<String, String> testMap = new HashMap<>();
        testMap.put("header1", "concat('a','b')");
        testMap.put("header2", "toUpper('a')");

        Optional<Map<String, String>> input = Optional.of(testMap);

        assertThat(responseTransformerUnderTest.transformResponseHeaders(input)).contains(new AbstractMap.SimpleEntry<String, String>("header1", "CONCAT"));
        assertThat(responseTransformerUnderTest.transformResponseHeaders(input)).contains(new AbstractMap.SimpleEntry<String, String>("header2", "UPPER"));
    }

    @Test
    public void itUsesTheResponseHeadersIfNoTransformSpecified(){
        //TODO
    }

    @Test
    public void itAllowsAnEmptySetOfHeadersResponse(){
        Optional<Map<String, String>> input = Optional.of(Collections.emptyMap());

        assertThat(responseTransformerUnderTest.transformResponseHeaders(input)).isEmpty();
    }

    @Test
    public void itHandlesMapsOfMaps() throws JsonProcessingException {

        Map<String, Object> testMap = new HashMap<>();
        testMap.put("attribute1", "concat('a','b')");

        Map<String, Object> nestedMap = new HashMap<>();
        nestedMap.put("nestedAttribute1", "concat('a','b')");
        nestedMap.put("nestedAttribute2", "toUpper('a')");

        testMap.put("attribute2", nestedMap);

        Optional<Map<String, Object>> input = Optional.of(testMap);


        //https://github.com/revinate/assertj-json
        DocumentContext json = JsonPath.parse(responseTransformerUnderTest.transformResponseBody(input));

        JsonPathAssert.assertThat(json).jsonPathAsString("$.attribute1").isEqualTo("CONCAT");
        JsonPathAssert.assertThat(json).jsonPathAsString("$.attribute2.nestedAttribute1").isEqualTo("CONCAT");
        JsonPathAssert.assertThat(json).jsonPathAsString("$.attribute2.nestedAttribute2").isEqualTo("UPPER");
    }

    @Test
    public void itHandlesAList() throws JsonProcessingException {

        Map<String, Object> testMap = new HashMap<>();
        testMap.put("attribute1", "concat('a','b')");

        List<String> nestedList = new ArrayList<>();
        nestedList.add("concat('a','b')");
        nestedList.add("toUpper('a')");

        testMap.put("attribute2", nestedList);

        Optional<Map<String, Object>> input = Optional.of(testMap);

        //https://github.com/revinate/assertj-json
        DocumentContext json = JsonPath.parse(responseTransformerUnderTest.transformResponseBody(input));

        JsonPathAssert.assertThat(json).jsonPathAsString("$.attribute1").isEqualTo("CONCAT");
        JsonPathAssert.assertThat(json).jsonPathAsString("$.attribute2[0]").isEqualTo("CONCAT");
        JsonPathAssert.assertThat(json).jsonPathAsString("$.attribute2[1]").isEqualTo("UPPER");
    }
}

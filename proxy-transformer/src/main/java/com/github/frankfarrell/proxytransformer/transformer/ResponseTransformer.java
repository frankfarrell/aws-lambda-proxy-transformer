package com.github.frankfarrell.proxytransformer.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.frankfarrell.proxytransformer.context.response.ResponseDocumentContextHolder;
import com.github.frankfarrell.proxytransformer.context.response.ResponseHeadersContextHolder;
import com.github.frankfarrell.proxytransformer.context.response.ResponseStatusCodeContextHolder;
import com.github.frankfarrell.proxytransformer.parser.ExpressionParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by ffarrell on 28/11/2017.
 */
public class ResponseTransformer {

    private final ObjectMapper objectMapper;
    private final ExpressionParser expressionParser;

    public ResponseTransformer(final ObjectMapper objectMapper, final ExpressionParser expressionParser) {
        this.objectMapper = objectMapper;
        this.expressionParser = expressionParser;
    }

    public Integer transformResponseStatusCode(final Optional<String> responseStatusCode){

        return responseStatusCode
                .map(s ->
                        Integer.valueOf((String) expressionParser.parseAndBuildFunction(s).apply(null)))
                .orElseGet(() -> Integer.valueOf(ResponseStatusCodeContextHolder.getContext()));
    }

    public Map<String, String> transformResponseHeaders(final Optional<Map<String, String>> responseHeadersOptional) {

        return responseHeadersOptional.map(stringStringMap -> stringStringMap
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (String) expressionParser.parseAndBuildFunction(entry.getValue()).apply(null))
                ))
                .orElseGet(ResponseHeadersContextHolder::getContext);
    }

    public String transformResponseBody(final Optional<Map<String, Object>> responseBody) throws JsonProcessingException {

        if(responseBody.isPresent()){
            final Map<String, Object> response = parseMapValues(responseBody.get());
            return objectMapper.writeValueAsString(response);
        }
        else {
            return objectMapper.writeValueAsString((ResponseDocumentContextHolder.getContext()));
        }
    }

    /*
    NB We allow nesting of maps, but if there is list we only allow a list of Strings.
    So for instance ["toUpper('a')", "toLower('B')"]
     */
    protected Map<String, Object> parseMapValues(Map<String, Object> mapToParse) {
        return mapToParse
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            if(entry.getValue() instanceof String){
                                return (String) expressionParser.parseAndBuildFunction((String)entry.getValue()).apply(null);
                            }
                            //TODO This needs rework
                            else if(entry.getValue() instanceof List){
                                List asList = ((List)entry.getValue());
                                return asList.stream()
                                        .map(value -> expressionParser.parseAndBuildFunction((String)value).apply(null))
                                        .collect(Collectors.toList());
                            }
                            //its a map
                            else if(entry.getValue() instanceof Map){
                                Map<String, Object> values = new HashMap<>();
                                values.putAll((Map)entry.getValue());
                                return parseMapValues(values);
                            }
                            else {
                                throw new RuntimeException("What is it??? " + entry.getValue().toString());
                            }
                        })
                );
    }

}

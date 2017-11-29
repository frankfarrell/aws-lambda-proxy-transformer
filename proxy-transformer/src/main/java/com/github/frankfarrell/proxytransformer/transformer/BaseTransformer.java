package com.github.frankfarrell.proxytransformer.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.frankfarrell.proxytransformer.parser.ExpressionParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class BaseTransformer {

    protected final ObjectMapper objectMapper;
    protected final ExpressionParser expressionParser;

    protected BaseTransformer(ObjectMapper objectMapper, ExpressionParser expressionParser) {
        this.objectMapper = objectMapper;
        this.expressionParser = expressionParser;
    }


    protected Optional<Map<String, String>> mapKeyValue(Optional<Map<String, String>> keyValue) {
        return keyValue.map(stringStringMap -> stringStringMap
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (String) expressionParser.parseAndBuildFunction(entry.getValue()).apply(null))
                ));
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
                                return expressionParser.parseAndBuildFunction((String)entry.getValue()).apply(null);
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

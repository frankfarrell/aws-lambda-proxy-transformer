package com.github.frankfarrell.proxytransformer.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.frankfarrell.proxytransformer.context.request.RequestDocumentContextHolder;
import com.github.frankfarrell.proxytransformer.context.request.RequestHeadersContextHolder;
import com.github.frankfarrell.proxytransformer.context.response.ResponseDocumentContextHolder;
import com.github.frankfarrell.proxytransformer.context.response.ResponseHeadersContextHolder;
import com.github.frankfarrell.proxytransformer.context.response.ResponseStatusCodeContextHolder;
import com.github.frankfarrell.proxytransformer.parser.ExpressionParser;

import java.util.Map;
import java.util.Optional;

public class ResponseTransformer extends BaseTransformer{


    public ResponseTransformer(final ObjectMapper objectMapper, final ExpressionParser expressionParser) {
        super(objectMapper, expressionParser);
    }

    public Integer transformResponseStatusCode(final Optional<String> responseStatusCode){

        return responseStatusCode.map(s ->
                        Integer.valueOf((String) expressionParser.parseAndBuildFunction(s).apply(null)))
                .orElseGet(() -> Integer.valueOf(ResponseStatusCodeContextHolder.getContext()));
    }

    public Map<String, String> transformResponseHeaders(final Optional<Map<String, String>> responseHeadersOptional) {
        return mapHeaders(responseHeadersOptional).orElseGet(ResponseHeadersContextHolder::getContext);
    }

    public String transformResponseBody(final Optional<Map<String, Object>> responseBody) throws JsonProcessingException {

        if(responseBody.isPresent()){
            final Map<String, Object> response = parseMapValues(responseBody.get());
            return objectMapper.writeValueAsString(response);
        }
        else {
            return objectMapper.writeValueAsString(ResponseDocumentContextHolder.getContext());
        }
    }
}

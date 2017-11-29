package com.github.frankfarrell.proxytransformer.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.frankfarrell.proxytransformer.context.request.RequestDocumentContextHolder;
import com.github.frankfarrell.proxytransformer.context.request.RequestHeadersContextHolder;
import com.github.frankfarrell.proxytransformer.parser.ExpressionParser;

import java.util.Map;
import java.util.Optional;

public class RequestTransformer extends BaseTransformer{

    public RequestTransformer(final ObjectMapper objectMapper, final ExpressionParser expressionParser) {
        super(objectMapper, expressionParser);
    }

    public Map<String, String> transformRequestHeaders(final Optional<Map<String, String>> requestHeadersOptional) {
        return mapHeaders(requestHeadersOptional).orElseGet(RequestHeadersContextHolder::getContext);
    }

    public String transformRequestBody(final Optional<Map<String, Object>> requestBody) throws JsonProcessingException {

        if(requestBody.isPresent()){
            final Map<String, Object> response = parseMapValues(requestBody.get());
            return objectMapper.writeValueAsString(response);
        }
        else {
            return objectMapper.writeValueAsString(RequestDocumentContextHolder.getContext());
        }
    }

}

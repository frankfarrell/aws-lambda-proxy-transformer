package com.github.frankfarrell.proxytransformer.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.frankfarrell.proxytransformer.config.UserPassTuple;
import com.github.frankfarrell.proxytransformer.context.request.RequestDocumentContextHolder;
import com.github.frankfarrell.proxytransformer.context.request.RequestHeadersContextHolder;
import com.github.frankfarrell.proxytransformer.context.request.RequestQueryParamsContextHolder;
import com.github.frankfarrell.proxytransformer.parser.ExpressionParser;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class RequestTransformer extends BaseTransformer{

    public RequestTransformer(final ObjectMapper objectMapper, final ExpressionParser expressionParser) {
        super(objectMapper, expressionParser);
    }

    public Map<String, String> transformRequestHeaders(final Optional<Map<String, String>> requestHeadersOptional) {
        return mapKeyValue(requestHeadersOptional).orElseGet(RequestHeadersContextHolder::getContext);
    }

    public Optional<String> transformRequestBody(final Optional<Map<String, Object>> requestBody) throws JsonProcessingException {

        if(requestBody.isPresent()){
            final Map<String, Object> response = parseMapValues(requestBody.get());
            return Optional.of(objectMapper.writeValueAsString(response));
        }
        else if(RequestDocumentContextHolder.getContext().isPresent()){
            return Optional.of(objectMapper.writeValueAsString(RequestDocumentContextHolder.getContext().get()));
        }
        else {
            return Optional.empty();
        }
    }

    public Map<String, String> transformRequestQueryParams(Optional<Map<String, String>> queryParamsToSend) {
        return mapKeyValue(queryParamsToSend).orElse(RequestQueryParamsContextHolder.getContext());
    }

    public Optional<UserPassTuple> transformRequestUsernamePassword(final Optional<String> requestUser, final Optional<String> requestPassword) {

        if (requestUser.isPresent() && requestPassword.isPresent()) {
            return Optional.of(
                    new UserPassTuple(
                            (String) expressionParser.parseAndBuildFunction(requestUser.get()).apply(null),
                            (String) expressionParser.parseAndBuildFunction(requestPassword.get()).apply(null)
                    ));
        } else {
            //We wont pass anything from original request by default
            return Optional.empty();
        }
    }
}

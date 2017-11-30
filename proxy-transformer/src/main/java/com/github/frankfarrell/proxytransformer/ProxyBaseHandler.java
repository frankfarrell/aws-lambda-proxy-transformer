package com.github.frankfarrell.proxytransformer;

import com.amazonaws.serverless.proxy.internal.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.internal.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.github.frankfarrell.proxytransformer.config.HttpMethod;
import com.github.frankfarrell.proxytransformer.config.MethodPathTuple;
import com.github.frankfarrell.proxytransformer.config.ProxyConfiguration;
import com.github.frankfarrell.proxytransformer.config.UserPassTuple;
import com.github.frankfarrell.proxytransformer.context.request.*;
import com.github.frankfarrell.proxytransformer.context.response.ResponseDocumentContextHolder;
import com.github.frankfarrell.proxytransformer.context.response.ResponseHeadersContextHolder;
import com.github.frankfarrell.proxytransformer.context.response.ResponseStatusCodeContextHolder;
import com.github.frankfarrell.proxytransformer.functions.DefaultBiFunctions;
import com.github.frankfarrell.proxytransformer.functions.DefaultFunctions;
import com.github.frankfarrell.proxytransformer.functions.DefaultVariables;
import com.github.frankfarrell.proxytransformer.parser.ExpressionParser;
import com.github.frankfarrell.proxytransformer.transformer.RequestTransformer;
import com.github.frankfarrell.proxytransformer.transformer.ResponseTransformer;
import com.jayway.jsonpath.Configuration;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

/*
Extend this class in your lambda
 */
public class ProxyBaseHandler implements RequestStreamHandler {

    private static final Logger log = LoggerFactory.getLogger(ProxyBaseHandler.class);

    protected final ObjectMapper objectMapper;
    private final Map<MethodPathTuple, ProxyConfiguration> proxyConfiguration;

    private final ExpressionParser expressionParser;

    private final ResponseTransformer responseTransformer;
    private final RequestTransformer requestTransformer;
    /*
    Default configuration, using built in functions and default config file.
     */
    protected ProxyBaseHandler(final ObjectMapper objectMapper) throws IOException {
        this(DefaultVariables.getDefaultSupplierFunctions(),
                DefaultFunctions.getDefaultFunctions(),
                DefaultBiFunctions.getDefaultBiFunctions(),
                objectMapper,
                "src/main/resources/default_config.json");
    }

    protected ProxyBaseHandler() throws IOException {
        this(DefaultVariables.getDefaultSupplierFunctions(),
                DefaultFunctions.getDefaultFunctions(),
                DefaultBiFunctions.getDefaultBiFunctions(),
                getDefaultObjectMapper(),
                "src/main/resources/default_config.json");
    }

    /*
    Probably the configuration you want to use to start with: default functions and you can specify the configuration file
     */
    protected ProxyBaseHandler(final String proxyConfigurationFilePath) throws IOException{
        this(DefaultVariables.getDefaultSupplierFunctions(),
                DefaultFunctions.getDefaultFunctions(),
                DefaultBiFunctions.getDefaultBiFunctions(),
                getDefaultObjectMapper(),
                proxyConfigurationFilePath);
    }

    /*
    Add your own custom functions
     */
    protected ProxyBaseHandler(final Map<String, Supplier<Object>> suppliers,
                               final Map<String, Function<Object, Object>> functions,
                               final Map<String, BiFunction<Object, Object, Object>> biFunctions,
                               final String proxyConfigurationFilePath) throws IOException {
        this(suppliers, functions, biFunctions, getDefaultObjectMapper(), proxyConfigurationFilePath);
    }

    /*
    Add your own customer functions and your jackson object mapper. Only use if you know what youre doing!
     */
    protected ProxyBaseHandler(final Map<String, Supplier<Object>> suppliers,
                               final Map<String, Function<Object, Object>> functions,
                               final Map<String, BiFunction<Object, Object, Object>> biFunctions,
                               final ObjectMapper objectMapper,
                               final String proxyConfigurationFilePath) throws IOException {

        this.expressionParser = new ExpressionParser(suppliers, functions, biFunctions);
        this.objectMapper = objectMapper;

        final List<ProxyConfiguration> allProxyConfigurations =
                this.objectMapper.readValue(new File(proxyConfigurationFilePath), new TypeReference<List<ProxyConfiguration>>(){});
        this.proxyConfiguration = allProxyConfigurations.stream()
                .collect(Collectors.toMap(proxyConifg ->
                                new MethodPathTuple(proxyConifg.inputMethod, proxyConifg.inputPathPattern), Function.identity()));

        this.responseTransformer = new ResponseTransformer(objectMapper, expressionParser);
        this.requestTransformer = new RequestTransformer(objectMapper, expressionParser);
    }

    public static ObjectMapper getDefaultObjectMapper() {
        return new ObjectMapper()
                .registerModules(
                        new JavaTimeModule()
                                .addDeserializer(LocalDate.class,
                                        new LocalDateDeserializer(DateTimeFormatter.BASIC_ISO_DATE)),
                        new Jdk8Module())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    @Override
    public void handleRequest(final InputStream input,
                              final OutputStream output,
                              final Context context) throws IOException {

        final AwsProxyRequest request = this.objectMapper.readValue(input, AwsProxyRequest.class);

        final HttpMethod currentHttpMethod = HttpMethod.forValue(request.getHttpMethod());
        final String currentPath =request.getPath();

        final ProxyConfiguration salientProxyConfiguration = this.proxyConfiguration
                .keySet()
                .stream()
                .filter(x -> x.httpMethod.equals(currentHttpMethod) && currentPath.matches(x.pathPattern))
                .map(proxyConfiguration::get)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No proxy configuration for path " + currentPath));


        final Map<String, String> matchedGroups = getMatchedPathGroups(currentPath, salientProxyConfiguration.inputPathPattern);

        RequestPathContextHolder.setContext(new RequestPath(currentPath, matchedGroups));
        RequestHeadersContextHolder.setContext(request.getHeaders());
        if(request.getBody() != null){
            RequestDocumentContextHolder.setContext(Configuration.defaultConfiguration().jsonProvider().parse(request.getBody()));
        }
        RequestMethodContextHolder.setContext(request.getHttpMethod());
        RequestQueryParamsContextHolder.setContext(request.getQueryStringParameters());


        try {
            doRequest(salientProxyConfiguration);
        } catch (UnirestException e) {
            //TODO Improve this
            log.error("Unirest error", e);
        }

        //At the end ->
        final AwsProxyResponse resp = new AwsProxyResponse();
        resp.setBody(responseTransformer.transformResponseBody(salientProxyConfiguration.responseBody));
        resp.setHeaders(responseTransformer.transformResponseHeaders(salientProxyConfiguration.responseHeaders));
        resp.setStatusCode(responseTransformer.transformResponseStatusCode(salientProxyConfiguration.responseStatusCode));
        this.objectMapper.writeValue(output, resp);
        output.close();
    }

    private void doRequest(final ProxyConfiguration salientProxyConfiguration) throws UnirestException, JsonProcessingException {
        final HttpMethod methodToCall = salientProxyConfiguration.destinationMethod;
        final String pathToCall = (String)expressionParser.parseAndBuildFunction(salientProxyConfiguration.destinationPath).apply(null);

        final HttpResponse<String> response;
        switch(methodToCall){
            case GET:
                response = doRequestWithoutBody(Unirest.get(pathToCall), salientProxyConfiguration);
                break;
            case POST:
                response = doRequestWithBody(Unirest.post(pathToCall), salientProxyConfiguration);
                break;
            case DELETE:
                response = doRequestWithBody(Unirest.delete(pathToCall), salientProxyConfiguration);
                break;
            case PUT:
                response = doRequestWithBody(Unirest.put(pathToCall), salientProxyConfiguration);
                break;
            case PATCH:
                response = doRequestWithBody(Unirest.patch(pathToCall), salientProxyConfiguration);
                break;
            case OPTIONS:
                response = doRequestWithBody(Unirest.options(pathToCall), salientProxyConfiguration);
                break;
            case HEAD:
                response = doRequestWithoutBody(Unirest.head(pathToCall), salientProxyConfiguration);
                break;
            default:
                throw new RuntimeException("This is impossible");
        }

        ResponseHeadersContextHolder.setContext(response.getHeaders().entrySet()
                .stream().collect(Collectors.toMap(Map.Entry::getKey,
                        e -> e.getValue().stream().collect(joining(";")))));
        ResponseDocumentContextHolder.setContext(response.getBody());
        ResponseStatusCodeContextHolder.setContext(response.getStatus());
    }

    private HttpResponse<String> doRequestWithBody(final HttpRequestWithBody request, final ProxyConfiguration salientProxyConfiguration) throws UnirestException, JsonProcessingException {
        final Map<String, String> headers = requestTransformer.transformRequestHeaders(salientProxyConfiguration.headersToSend);

        //Optional request properties
        final Optional<String> requestBody = requestTransformer.transformRequestBody(salientProxyConfiguration.bodyToSend);
        final Map<String, String> queryParams = requestTransformer.transformRequestQueryParams(salientProxyConfiguration.queryParamsToSend);
        final Optional<UserPassTuple> requestUserPass = requestTransformer.transformRequestUsernamePassword(salientProxyConfiguration.requestUsername, salientProxyConfiguration.requestPassword);

        request.headers(headers);
        requestBody.ifPresent(request::body);
        queryParams.forEach(request::queryString);
        requestUserPass.ifPresent(userPass -> request.basicAuth(userPass.username, userPass.password));

        return request.asString();
    }

    private HttpResponse<String> doRequestWithoutBody(final HttpRequest request, final ProxyConfiguration salientProxyConfiguration) throws UnirestException {
        final Map<String, String> headers = requestTransformer.transformRequestHeaders(salientProxyConfiguration.headersToSend);

        //Optional request properties
        final Map<String, String> queryParams = requestTransformer.transformRequestQueryParams(salientProxyConfiguration.queryParamsToSend);
        final Optional<UserPassTuple> requestUserPass = requestTransformer.transformRequestUsernamePassword(salientProxyConfiguration.requestUsername, salientProxyConfiguration.requestPassword);

        request.headers(headers);
        queryParams.forEach(request::queryString);
        requestUserPass.ifPresent(userPass -> request.basicAuth(userPass.username, userPass.password));

        return request.asString();
    }

    //TODO Is there a nice way to do this?
    protected Map<String, String> getMatchedPathGroups(final String currentPath, final String inputPathPattern) {
        final Matcher matcher = Pattern.compile(inputPathPattern).matcher(currentPath);

        matcher.matches();
        final Map<String, String> matchedGroups = new HashMap<>();
        //groupCount does not include the full match
        for(Integer i=0; i<matcher.groupCount()+1; i++){
            matchedGroups.put(i.toString(), matcher.group(i));
        }
        return matchedGroups;
    }
}

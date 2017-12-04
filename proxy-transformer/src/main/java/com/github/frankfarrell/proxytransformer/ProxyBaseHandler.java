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
import java.util.*;
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
public class ProxyBaseHandler{

    private static final Logger log = LoggerFactory.getLogger(ProxyBaseHandler.class);

    private static final ObjectMapper DEFAULT_OBJECT_MAPPER =
            new ObjectMapper()
                    .registerModules(
                            new JavaTimeModule()
                                    .addDeserializer(LocalDate.class,
                                            new LocalDateDeserializer(DateTimeFormatter.BASIC_ISO_DATE)),
                            new Jdk8Module())
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    protected final ObjectMapper objectMapper;
    private final Map<MethodPathTuple, ProxyConfiguration> proxyConfiguration;

    private final ExpressionParser expressionParser;

    private final ResponseTransformer responseTransformer;
    private final RequestTransformer requestTransformer;
    private final Unirest unirest;

    public static ObjectMapper getDefaultObjectMapper(){
        return DEFAULT_OBJECT_MAPPER;
    }

    /*
    Probably the configuration you want to use to start with: default functions and you can specify the configuration file
     */
    public ProxyBaseHandler(final File proxyConfigurationFilePath) throws IOException{
        this(Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                DEFAULT_OBJECT_MAPPER,
                proxyConfigurationFilePath, new Unirest());
    }

    /*
    Add your own custom functions
     */
    public ProxyBaseHandler(final Map<String, Supplier<Object>> suppliers,
                               final Map<String, Function<Object, Object>> functions,
                               final Map<String, BiFunction<Object, Object, Object>> biFunctions,
                               final File proxyConfigurationFilePath) throws IOException {
        this(suppliers,
                functions,
                biFunctions,
                DEFAULT_OBJECT_MAPPER,
                proxyConfigurationFilePath,
                new Unirest());
    }

    /*
    Add your own customer functions and your jackson object mapper. Only use if you know what youre doing!
     */
    public ProxyBaseHandler(final Map<String, Supplier<Object>> suppliers,
                               final Map<String, Function<Object, Object>> functions,
                               final Map<String, BiFunction<Object, Object, Object>> biFunctions,
                               final ObjectMapper objectMapper,
                               final File proxyConfigurationFile,
                               final Unirest unirest) throws IOException {
        //Load built in functions
        suppliers.putAll(DefaultVariables.getDefaultSupplierFunctions());
        functions.putAll(DefaultFunctions.getDefaultFunctions());
        biFunctions.putAll(DefaultBiFunctions.getDefaultBiFunctions());

        this.expressionParser = new ExpressionParser(suppliers, functions, biFunctions);
        this.objectMapper = objectMapper;

        final List<ProxyConfiguration> allProxyConfigurations =
                this.objectMapper.readValue(proxyConfigurationFile, new TypeReference<List<ProxyConfiguration>>(){});
        this.proxyConfiguration = allProxyConfigurations.stream()
                .collect(Collectors.toMap(proxyConifg ->
                                new MethodPathTuple(proxyConifg.inputMethod, proxyConifg.inputPathPattern), Function.identity()));

        this.responseTransformer = new ResponseTransformer(objectMapper, expressionParser);
        this.requestTransformer = new RequestTransformer(objectMapper, expressionParser);
        this.unirest = unirest;
    }

    public ProxyResponse handleRequest(final ProxyRequest request) throws IOException {

        //TODO Remove this
        final HttpMethod currentHttpMethod = request.currentHttpMethod;
        final String currentPath =request.currentPath;

        //TODO In method, add tests
        final ProxyConfiguration salientProxyConfiguration = this.proxyConfiguration
                .keySet()
                .stream()
                .filter(x -> x.httpMethod.equals(currentHttpMethod) && currentPath.matches(x.pathPattern))
                .map(proxyConfiguration::get)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No proxy configuration for path " + currentPath));


        final Map<String, String> matchedGroups = getMatchedPathGroups(currentPath, salientProxyConfiguration.inputPathPattern);

        RequestPathContextHolder.setContext(new RequestPath(currentPath, matchedGroups));
        RequestHeadersContextHolder.setContext(request.headers);
        if(request.body.isPresent()){
            RequestDocumentContextHolder.setContext(Configuration.defaultConfiguration().jsonProvider().parse(request.body.get()));
        }
        else{
            //?
        }
        RequestMethodContextHolder.setContext(request.currentHttpMethod.value);
        RequestQueryParamsContextHolder.setContext(request.queryParams);


        try {
            doRequest(salientProxyConfiguration);
        } catch (UnirestException e) {
            //TODO Return a 503 error to client
            log.error("Unirest error", e);
        }

        return new ProxyResponse(responseTransformer.transformResponseBody(salientProxyConfiguration.responseBody),
                responseTransformer.transformResponseHeaders(salientProxyConfiguration.responseHeaders),
                responseTransformer.transformResponseStatusCode(salientProxyConfiguration.responseStatusCode));
    }

    //TODO Put these methods in another ProxyService class
    @SuppressWarnings("AccessStaticViaInstance")
    private void doRequest(final ProxyConfiguration salientProxyConfiguration) throws UnirestException, JsonProcessingException {
        final HttpMethod methodToCall = salientProxyConfiguration.destinationMethod;
        final String pathToCall = (String)expressionParser.parseAndBuildFunction(salientProxyConfiguration.destinationPath).apply(null);

        final HttpResponse<String> response;
        switch(methodToCall){
            case GET:
                response = doRequestWithoutBody(unirest.get(pathToCall), salientProxyConfiguration);
                break;
            case POST:
                response = doRequestWithBody(unirest.post(pathToCall), salientProxyConfiguration);
                break;
            case DELETE:
                response = doRequestWithBody(unirest.delete(pathToCall), salientProxyConfiguration);
                break;
            case PUT:
                response = doRequestWithBody(unirest.put(pathToCall), salientProxyConfiguration);
                break;
            case PATCH:
                response = doRequestWithBody(unirest.patch(pathToCall), salientProxyConfiguration);
                break;
            case OPTIONS:
                response = doRequestWithBody(unirest.options(pathToCall), salientProxyConfiguration);
                break;
            case HEAD:
                response = doRequestWithoutBody(unirest.head(pathToCall), salientProxyConfiguration);
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
    //TODO Put this method as static in MethodPathTuple?
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

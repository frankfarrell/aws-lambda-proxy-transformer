package com.github.frankfarrell.proxytransformer.functions;

import com.github.frankfarrell.proxytransformer.context.request.*;
import com.github.frankfarrell.proxytransformer.context.response.ResponseHeadersContextHolder;
import com.jayway.jsonpath.JsonPath;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by ffarrell on 24/11/2017.
 */
public class DefaultFunctions {

    public static final Map<String, Function<Object, Object>> getDefaultFunctions(){
        final Map<String, Function<Object, Object>> functionMap = new HashMap<>();

        functionMap.put("toUpper",toUpper());
        functionMap.put("toLower",toLower());
        functionMap.put("split",split());

        functionMap.put("requestJsonPath", requestJsonPath());
        functionMap.put("responseJsonPath", responseJsonPath());
        functionMap.put("requestHeader", requestHeader());
        functionMap.put("responseHeader", responseHeader());
        functionMap.put("requestQueryParam", requestQueryParam());
        functionMap.put("requestPathGroup", requestPathGroup());
        return functionMap;
    }

    /*
    String functions
     */

    public static  Function<Object, Object> toUpper(){
        return x -> ((String) x).toUpperCase();
    }

    public static  Function<Object, Object> toLower(){
        return x -> ((String) x).toLowerCase();
    }

    //Returns a list
    public static  Function<Object,Object> split(){
        return a-> Arrays.asList(((String)a).split(""));
    }

    /*
    Context functions
     */
    public static Function<Object,Object> requestJsonPath(){
        return jsonPath -> JsonPath.read(RequestDocumentContextHolder.getContext(), (String)jsonPath);
    }

    public static Function<Object,Object> responseJsonPath(){
        return jsonPath -> JsonPath.read(RequestDocumentContextHolder.getContext(), (String)jsonPath);
    }

    public static Function<Object,Object> requestHeader(){
        return headerName -> RequestHeadersContextHolder.getContext().get((String)headerName);
    }

    public static Function<Object,Object> responseHeader(){
        return headerName -> ResponseHeadersContextHolder.getContext().get((String)headerName);
    }

    public static Function<Object,Object> requestQueryParam(){
        return headerName -> RequestQueryParamsContextHolder.getContext().get((String)headerName);
    }

    public static Function<Object,Object> requestPathGroup(){
        return groupNumber -> RequestPathContextHolder.getContext().matchedGroups.get((String) groupNumber);
    }



}

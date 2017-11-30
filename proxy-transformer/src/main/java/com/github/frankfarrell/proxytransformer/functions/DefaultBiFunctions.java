package com.github.frankfarrell.proxytransformer.functions;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class DefaultBiFunctions {

    public static Map<String, BiFunction<Object, Object, Object>> getDefaultBiFunctions(){
        final Map<String, BiFunction<Object, Object, Object>> functionMap = new HashMap<>();
        functionMap.put("concat", concat());
        functionMap.put("substring", substring());

        functionMap.put("plus", plus());
        functionMap.put("minus", minus());
        functionMap.put("multiply", multiply());
        functionMap.put("divide", divide());
        functionMap.put("max", max());
        functionMap.put("min", min());
        return functionMap;

    }


    /*
    String functions
    */
    public static BiFunction<Object, Object, Object> concat(){
        return (a, b) -> (String)a+ (String)b;
    }

    public static  BiFunction<Object, Object, Object> substring(){
        return (a, b) -> ((String)a).substring(((Double)b).intValue());
    }

    /*
    Math functions
     */
    public static BiFunction<Object, Object, Object> plus(){
        return (a,b) -> (Double)a + (Double)b;
    }

    public static BiFunction<Object, Object, Object> minus(){
        return (a,b) -> (Double)a - (Double)b;
    }

    public static BiFunction<Object, Object, Object> multiply(){
        return (a,b) -> (Double)a * (Double)b;
    }

    public static BiFunction<Object, Object, Object> divide(){
        return (a,b) -> (Double)a / (Double)b;
    }

    public static BiFunction<Object, Object, Object> max(){
        return (a,b) -> Math.max((Double)a,(Double)b);
    }

    public static BiFunction<Object, Object, Object> min(){
        return (a,b) -> Math.min((Double)a,(Double)b);
    }

}

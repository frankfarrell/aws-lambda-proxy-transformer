package com.github.frankfarrell.proxytransformer.parser;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ExpressionParser {

    private static final String IDENTITY_FUNCTION = "identity";

    private final Map<String, Supplier<Object>> supplierFunctions;
    private final Map<String, Function<Object, Object>> functions;
    private final Map<String, BiFunction<Object, Object, Object>> biFunctions;

    private final Map<String, Function<Void,Object>> functionCache;

    public ExpressionParser(final Map<String, Supplier<Object>> supplierFunctions,
                            final Map<String, Function<Object, Object>> functions,
                            final Map<String, BiFunction<Object, Object, Object>> biFunctions) {
        this.supplierFunctions = supplierFunctions;
        this.functions = functions;
        this.biFunctions = biFunctions;
        this.functionCache = new HashMap<>();
    }

    public Function<Void, Object> parseAndBuildFunction(final String functionExpression){
        if(functionCache.containsKey(functionExpression)){
            return functionCache.get(functionExpression);
        }
        else{
            final Function<Void, Object> parsedFunction = buildFunction(parseArgs(functionExpression));
            functionCache.put(functionExpression, parsedFunction);
            return parsedFunction;
        }
    }

    protected FunctionData parseArgs(String function) {

        if(!function.contains("(")){
            return new FunctionData(IDENTITY_FUNCTION,
                    Collections.<FunctionData>singletonList(new FunctionData(function, Collections.EMPTY_LIST)));
        }
        else{
            final String[] functionNameAndArgs = function.split("\\(", 2);

            final String functionName = functionNameAndArgs[0];
            final String args = functionNameAndArgs[1].replaceAll("\\)$", "");

            final List<FunctionData> argsParsed;
            if(args.trim().equals("")){
                argsParsed = Collections.EMPTY_LIST;
            }
            else{
                int commaIndex = getIndexOfCommaSeparator(args);
                if(commaIndex >0){
                    argsParsed = Arrays.asList(parseArgs(args.substring(0, commaIndex)), parseArgs(args.substring(commaIndex+1)));
                }
                else {
                    argsParsed = Collections.singletonList(parseArgs(args));
                }
            }

            return new FunctionData(functionName, argsParsed);
        }
    }

    protected int getIndexOfCommaSeparator(final String args) {

        int leftBracketCount = 0;
        int rightBracketCount = 0;

        final char[] characters = args.toCharArray();
        for(int i=0;i<args.length();i++){
            switch (characters[i]){
                case '(':
                    leftBracketCount ++;
                    break;
                case ')':
                    rightBracketCount ++;
                    break;
                case ',':
                    if(leftBracketCount == rightBracketCount ||
                            leftBracketCount == (rightBracketCount + 1)){ //Case where there are nested statements?
                        return i;
                    }
                    break;
                default:
                    break;
            }
        }
        return -1;
    }

    //TODO Change this to supplier
    protected Function<Void, Object> buildFunction(final FunctionData parsedArguments) {

        //Return the parsed value
        if(parsedArguments.functionName.equalsIgnoreCase(IDENTITY_FUNCTION)){
            return (z) -> handleValue(parsedArguments.parameters.get(0).functionName.trim());
        }
        //Eg accessing variables
        else if(parsedArguments.parameters.size() == 0){
            return (z) -> supplierFunctions.get(parsedArguments.functionName.trim()).get();
        }
        else if(parsedArguments.parameters.size() ==1){

            return (z) -> buildFunction(parsedArguments.parameters.get(0))
                    .andThen(functions.get(parsedArguments.functionName.trim())).apply(null);
        }
        else if(parsedArguments.parameters.size() ==2){
            final Function<Void, Object> left = buildFunction(parsedArguments.parameters.get(0));
            final Function<Void, Object> right = buildFunction(parsedArguments.parameters.get(1));

            return (z) -> biFunctions.get(parsedArguments.functionName.trim())
                    .apply(left.apply(null), right.apply(null));
        }
        else{
            throw new RuntimeException("We dont handle functions with more than 2 args");
        }
    }

    //As it is in the input
    protected Object handleValue(final String fullValue) {

        final String trimmedValue = fullValue.trim();

        if (trimmedValue.trim().startsWith("'")){
            return trimmedValue.replaceAll("'", "");
        }
        else if(trimmedValue.matches("^\\d+$")){
            return Double.valueOf(trimmedValue);
        }
        else if(trimmedValue.equalsIgnoreCase("true") || trimmedValue.equalsIgnoreCase("false")){
            return Boolean.valueOf(trimmedValue);
        }
        //Yes we don't handle nested lists yet -> TODO Add note to limitations in readme
        else if(trimmedValue.matches("^\\[.*\\]$")){
            return Arrays.stream(trimmedValue.substring(1, trimmedValue.length() - 2).split(",")).map(this::handleValue).collect(Collectors.toList());
        }
        else{
            //Its just a plain old string without quotes
            return trimmedValue;
        }
    }
}

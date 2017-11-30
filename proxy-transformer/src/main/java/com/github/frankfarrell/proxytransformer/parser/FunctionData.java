package com.github.frankfarrell.proxytransformer.parser;

import java.util.List;

public class FunctionData {
    public final String functionName;
    public final List<FunctionData> parameters;

    public FunctionData(final String functionName, final List<FunctionData> parameters) {
        this.functionName = functionName;
        this.parameters = parameters;
    }
}
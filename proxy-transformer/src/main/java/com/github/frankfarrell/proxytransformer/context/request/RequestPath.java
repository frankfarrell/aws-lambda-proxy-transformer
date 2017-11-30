package com.github.frankfarrell.proxytransformer.context.request;

import java.util.Map;

public class RequestPath {

    public final String path;

    public final Map<String, String> matchedGroups;

    public RequestPath(final String path,
                       final Map<String, String> matchedGroups) {
        this.path = path;
        this.matchedGroups = matchedGroups;
    }
}

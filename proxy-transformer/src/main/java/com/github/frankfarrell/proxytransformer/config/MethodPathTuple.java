package com.github.frankfarrell.proxytransformer.config;

public final class MethodPathTuple {

    public final HttpMethod httpMethod;
    public final String pathPattern;

    public MethodPathTuple(final HttpMethod httpMethod, final String pathPattern) {
        this.httpMethod = httpMethod;
        this.pathPattern = pathPattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodPathTuple that = (MethodPathTuple) o;

        return httpMethod == that.httpMethod &&
                (pathPattern != null ?
                        pathPattern.equals(that.pathPattern) :
                        that.pathPattern == null);
    }

    @Override
    public int hashCode() {
        int result = httpMethod != null ? httpMethod.hashCode() : 0;
        result = 31 * result + (pathPattern != null ? pathPattern.hashCode() : 0);
        return result;
    }
}

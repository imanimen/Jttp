package com.imanimen.jttpserver.http;

public enum HttpMethod {
    HEAD,
    GET,
    POST,
    PUT,
    QUERY,
    DELETE;

    public static int MAX_LENGTH = 1024;

    static {
        int tempMaxLength = -1;
        for (HttpMethod httpMethod : HttpMethod.values()) {
            if (httpMethod.name().length() > tempMaxLength) {
                tempMaxLength = httpMethod.name().length();
            }
        }
        MAX_LENGTH = tempMaxLength;
    }
}

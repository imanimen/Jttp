package com.imanimen.jttpserver.http;

public class HttpRequest extends HttpMessage {

    private HttpMethod method;
    private String target;
    private String version;

    HttpRequest() {

    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }


}

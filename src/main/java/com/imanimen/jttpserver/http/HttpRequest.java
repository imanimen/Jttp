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

    public void setMethod(String methodName) throws HttpParsingException{
        for (HttpMethod httpMethod: HttpMethod.values()) {
            if (httpMethod.name().equals(methodName)) {
                this.method = httpMethod;
            }
        }
        throw new HttpParsingException(
                HttpStatusCode.SERVER_ERROR_501_NOT_IMPLEMENTED
        );
    }


}

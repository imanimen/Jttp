package com.imanimen.jttpserver.http;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpParserTest {

    private static HttpParser httpParser;

    @BeforeAll
    public static void beforeClass() {
        httpParser = new HttpParser();
    }
    @Test
    void parseHttpRequest() {
    }
}
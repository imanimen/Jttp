package com.imanimen.jttpserver.http;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class  HttpParserTest {

    private static HttpParser httpParser;

    @BeforeAll
    public static void beforeClass() {
        httpParser = new HttpParser();
    }
    @Test
    void parseHttpRequest()  {
        HttpRequest request = null;
        try {
            request = httpParser.parseHttpRequest(
                    generateValidGETTestCase()
            );

        } catch (HttpParsingException e) {
            fail(e);
        }

        assertEquals(HttpMethod.GET, request.getMethod());
    }

    @Test
    void parseHttpBadRequest() {
        try {
            HttpRequest request = httpParser.parseHttpRequest(
                    generateInValidGETTestCase()
            );
            fail();
        } catch (HttpParsingException e) {
            assertEquals(HttpStatusCode.SERVER_ERROR_501_NOT_IMPLEMENTED, e.getErrorCode());
        }
    }


    @Test
    void parseHttpBadRequestMethodMaxLength() {
        try {
            HttpRequest request = httpParser.parseHttpRequest(
                    generateInValidGETTestCaseMaxLength()
            );
            fail();
        } catch (HttpParsingException e) {
            assertEquals(HttpStatusCode.SERVER_ERROR_501_NOT_IMPLEMENTED, e.getErrorCode());
        }
    }

    @Test
    void parseHttpBadRequestInvalidRequestLineItems() {
        try {
            HttpRequest request = httpParser.parseHttpRequest(
                    generateInValidGETRequestLineInvalid()
            );
            fail();
        } catch (HttpParsingException e) {
            assertEquals(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST, e.getErrorCode());
        }
    }

    @Test
    void parseHttpBadRequestInvalidRequestLineMissing() {
        try {
            HttpRequest request = httpParser.parseHttpRequest(
                    generateInValidRequestLineMissing()
            );
            fail();
        } catch (HttpParsingException e) {
            assertEquals(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST, e.getErrorCode());
        }
    }

    @Test
    void parseHttpBadRequestInvalidRequestLineMissingLF() {
        try {
            HttpRequest request = httpParser.parseHttpRequest(
                    generateInValidGETRequestLineInvalidOnlyCRnoLF()
            );
            fail();
        } catch (HttpParsingException e) {
            assertEquals(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST, e.getErrorCode());
        }
    }

    private InputStream generateValidGETTestCase() {
        String rawHttpRequest = "GET / HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Connection: keep-alive\r\n" +
                "sec-ch-ua: \"Google Chrome\";v=\"149\", \"Chromium\";v=\"149\", \"Not)A;Brand\";v=\"24\"\r\n" +
                "sec-ch-ua-mobile: ?0\r\n" +
                "sec-ch-ua-platform: \"macOS\"\r\n" +
                "Upgrade-Insecure-Requests: 1\r\n" +
                "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36\r\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7\r\n" +
                "Sec-Fetch-Site: none\r\n" +
                "Sec-Fetch-Mode: navigate\r\n" +
                "Sec-Fetch-User: ?1\r\n" +
                "Sec-Fetch-Dest: document\r\n" +
                "Accept-Encoding: gzip, deflate, br, zstd\r\n" +
                "Accept-Language: en-US,en;q=0.9,fa;q=0.8,ar;q=0.7,fr;q=0.6\r\n" +
                "Cookie: Phpstorm-e23b83ee=e9b0b55c-7724-483f-93b4-b23824eeb394\r\n" +
                "\r\n";

        return new ByteArrayInputStream(
                rawHttpRequest.getBytes(
                        StandardCharsets.US_ASCII
                )
        );
    }

    private InputStream generateInValidGETTestCase() {
        String rawHttpRequest = "GeTTT / HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Cookie: Phpstorm-e23b83ee=e9b0b55c-7724-483f-93b4-b23824eeb394\r\n" +
                "\r\n";

        return new ByteArrayInputStream(
                rawHttpRequest.getBytes(
                        StandardCharsets.US_ASCII
                )
        );
    }

    private InputStream generateInValidGETTestCaseMaxLength() {
        String rawHttpRequest = "GETTTTTT / HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Cookie: Phpstorm-e23b83ee=e9b0b55c-7724-483f-93b4-b23824eeb394\r\n" +
                "\r\n";

        return new ByteArrayInputStream(
                rawHttpRequest.getBytes(
                        StandardCharsets.US_ASCII
                )
        );
    }

    private InputStream generateInValidGETRequestLineInvalid() {
        String rawHttpRequest = "GET / AAAAAA HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "\r\n";

        return new ByteArrayInputStream(
                rawHttpRequest.getBytes(
                        StandardCharsets.US_ASCII
                )
        );
    }

    private InputStream generateInValidRequestLineMissing() {
        String rawHttpRequest = "\r\n" +
                "Host: localhost:8080\r\n" +
                "\r\n";

        return new ByteArrayInputStream(
                rawHttpRequest.getBytes(
                        StandardCharsets.US_ASCII
                )
        );
    }

    private InputStream generateInValidGETRequestLineInvalidOnlyCRnoLF() {
        String rawHttpRequest = "GET / AAAAAA HTTP/1.1\r" + // <---- no Line Feed ---->
                "Host: localhost:8080\r\n" +
                "Cookie: Phpstorm-e23b83ee=e9b0b55c-7724-483f-93b4-b23824eeb394\r\n" +
                "\r\n";

        return new ByteArrayInputStream(
                rawHttpRequest.getBytes(
                        StandardCharsets.US_ASCII
                )
        );
    }

}
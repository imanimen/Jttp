# JTTP — Learning Guide

JTTP (Java Tiny HTTP Server) is a minimal HTTP server built from scratch in Java. It is an **educational project** designed to teach low-level networking, the HTTP protocol, and server architecture.

## Table of Contents

- [Project Structure](#project-structure)
- [Architecture Overview](#architecture-overview)
- [Entry Point](#entry-point)
- [Configuration Layer](#configuration-layer)
- [Core Server Runtime](#core-server-runtime)
- [HTTP Protocol Layer](#http-protocol-layer)
- [Utility Layer](#utility-layer)
- [Build & Run](#build--run)
- [Testing](#testing)
- [Current State & Next Steps](#current-state--next-steps)
- [Key Concepts to Learn](#key-concepts-to-learn)

---

## Project Structure

```
src/
├── main/java/com/imanimen/jttpserver/
│   ├── JttpServer.java              # Entry point (main)
│   ├── config/
│   │   ├── Configuration.java           # POJO: port + webroot
│   │   ├── ConfigurationManager.java    # Singleton: loads jttp.json
│   │   └── JttpConfigurationException.java
│   ├── core/
│   │   ├── ServerListenerThread.java        # Accepts socket connections
│   │   └── JttpConnectionWorkerThread.java  # Handles one connection
│   ├── http/
│   │   ├── HttpMessage.java             # Abstract base (request/response)
│   │   ├── HttpMethod.java              # Enum: GET, POST, PUT, DELETE
│   │   ├── HttpParser.java              # Parses raw bytes → HttpRequest
│   │   ├── HttpParsingException.java    # Exception with status code
│   │   ├── HttpRequest.java             # Request model
│   │   └── HttpStatusCode.java          # Status code enum
│   └── util/
│       └── JxUtil.java              # Jackson JSON facade
└── main/resources/
    └── jttp.json                     # Config file (port: 8080, webroot: /tmp)

test/java/com/imanimen/jttpserver/http/
└── HttpParserTest.java              # Unit test for the parser
```

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────┐
│                   JttpServer (main)                  │
│  Loads config → creates ServerListenerThread → start │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│             ServerListenerThread (Thread)             │
│  Binds ServerSocket → loop: accept() → spawn worker  │
└──────────────────────┬──────────────────────────────┘
                       │  (one per connection)
┌──────────────────────▼──────────────────────────────┐
│         JttpConnectionWorkerThread (Thread)           │
│  Read InputStream → parse HTTP → serve files → write │
│  response back to OutputStream                        │
└─────────────────────────────────────────────────────┘
```

JTTP uses a **thread-per-connection** model:
1. `ServerListenerThread` runs an infinite loop, calling `serverSocket.accept()`.
2. For each incoming connection, it creates a new `JttpConnectionWorkerThread`.
3. Each worker reads the raw HTTP request from the socket, processes it, and writes a response.

---

## Entry Point

### `JttpServer.java`

```java
public static void main(String[] args) {
    ConfigurationManager.getInstance().loadConfigurationFile("src/main/resources/jttp.json");
    Configuration conf = ConfigurationManager.getInstance().getCurrentConfiguration();
    ServerListenerThread serverListenerThread = new ServerListenerThread(conf.getPort(), conf.getWebroot());
    serverListenerThread.start();
}
```

**Flow:**
1. Loads JSON config via `ConfigurationManager` (singleton).
2. Reads `port` and `webroot` from `Configuration` POJO.
3. Creates and starts a `ServerListenerThread`.

---

## Configuration Layer

### `Configuration.java` — POJO

```java
public class Configuration {
    private int port;     // server port (e.g., 8080)
    private String webroot; // filesystem path for serving files (e.g., /tmp)
}
```

A plain JavaBean with getters/setters. Jackson maps the JSON keys directly to these fields.

### `ConfigurationManager.java` — Singleton

Responsible for:
1. Reading a JSON file character-by-character into a `StringBuffer`.
2. Parsing the JSON string into a Jackson `JsonNode`.
3. Converting the `JsonNode` into a `Configuration` object via `JxUtil.fromJson()`.
4. Caching the configuration and providing `getCurrentConfiguration()`.

```java
ConfigurationManager.getInstance().loadConfigurationFile("src/main/resources/jttp.json");
Configuration conf = ConfigurationManager.getInstance().getCurrentConfiguration();
```

### `JttpConfigurationException.java`

A `RuntimeException` subclass for configuration errors (file not found, JSON parse failure, etc.).

### Config file — `src/main/resources/jttp.json`

```json
{
  "port": 8080,
  "webroot": "/tmp"
}
```

---

## Core Server Runtime

### `ServerListenerThread.java`

- Extends `Thread`.
- Constructor creates a `ServerSocket` on the configured port.
- `run()` loops while the socket is bound and not closed:
  - Calls `serverSocket.accept()` (blocks until a client connects).
  - Logs the client's IP.
  - Spawns a new `JttpConnectionWorkerThread` for each connection.
- Cleans up the `ServerSocket` in `finally`.

> **Learning point:** This is a classic multi-threaded server pattern. Each connection gets its own thread, allowing concurrent handling.

### `JttpConnectionWorkerThread.java`

- Extends `Thread`, receives a `Socket` in its constructor.
- `run()`:
  1. Gets `InputStream` and `OutputStream` from the socket.
  2. **Currently** writes a hardcoded HTML response (ignores the actual request).
  3. Closes streams and socket in `finally`.

**Current response format:**
```
HTTP/1.1 200 OK\r\n
Content-Length: <length>\r\n
\r\n
<html>...</html>\r\n\r\n
```

> **Learning point:** Notice the raw HTTP wire format — status line, headers, blank line, body. This is what all HTTP servers and clients exchange.

> **What's missing:** The worker currently ignores the request. The next step is to use `HttpParser` to read and understand the incoming request, then serve files from `webroot` instead of returning hardcoded HTML.

---

## HTTP Protocol Layer

### `HttpMessage.java` — Abstract base

Currently empty. Designed to be extended by both `HttpRequest` and a future `HttpResponse`.

### `HttpRequest.java`

```java
public class HttpRequest extends HttpMessage {
    private HttpMethod method;   // GET, POST, PUT, DELETE
    private String target;       // "/index.html", "/api/users", ...
    private String version;      // "HTTP/1.1"

    public void setMethod(String methodName) throws HttpParsingException {
        for (HttpMethod httpMethod : HttpMethod.values()) {
            if (httpMethod.name().equals(methodName)) {
                this.method = httpMethod;
                return;
            }
        }
        throw new HttpParsingException(HttpStatusCode.SERVER_ERROR_501_NOT_IMPLEMENTED);
    }
}
```

Uses package-private constructor — currently only `HttpParser` can create instances.

`setMethod()` validates the raw method string against the `HttpMethod` enum. Unknown methods (e.g., `FOO`, `PATCH`) throw `501 Not Implemented`. This means the parser rejects unsupported methods at parse time, not at request-handling time.

### `HttpMethod.java` — Enum

```java
public enum HttpMethod {
    GET,
    POST,
    PUT,
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
```

An enum of supported HTTP methods. `MAX_LENGTH` is computed at class-load time from the longest method name (used by `HttpParser` to reject oversized method tokens before even checking validity).

### `HttpStatusCode.java` — Enum

```java
public enum HttpStatusCode {
    CLIENT_ERROR_400_BAD_REQUEST(400, "Bad Request"),
    CLIENT_ERROR_405_METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    CLIENT_ERROR_414_URI_TOO_LONG(414, "URI Too Long"),
    SERVER_ERROR_500_INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    SERVER_ERROR_501_NOT_IMPLEMENTED(500, "Not Implemented"); // note: 501 has code 500 (bug)
}
```

Each constant holds `STATUS_CODE` and `MESSAGE`. Useful for building error responses.

> **Note:** `SERVER_ERROR_501_NOT_IMPLEMENTED` uses `500` instead of `501` — a small bug to fix.

### `HttpParser.java`

Reads raw bytes from an `InputStream` and builds an `HttpRequest`.

```java
public HttpRequest parseHttpRequest(InputStream inputStream) throws HttpParsingException {
    InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.US_ASCII);
    HttpRequest request = new HttpRequest();
    parseRequestLine(reader, request);
    parseHeaders(reader, request);
    parseBody(reader, request);
    return request;
}
```

**Three-stage parse:**
1. **Request Line** — reads until `\r\n` (CRLF). Extracts method, target, and validates structure.
2. **Headers** — empty stub.
3. **Body** — empty stub.

**Request line parsing logic:**
```java
while ((_byte = reader.read()) >= 0) {
    if (_byte == CR) {
        _byte = reader.read();
        if (_byte == LF) {
            if (!methodParsed || !requestTargetParsed) {
                throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
            }
            return;
        }
    }
    if (_byte == SP) {
        if (!methodParsed) {
            request.setMethod(buffer.toString());  // validates against HttpMethod enum
            methodParsed = true;
        } else if (!requestTargetParsed) {
            requestTargetParsed = true;
        } else {
            throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST); // too many spaces
        }
        buffer.delete(0, buffer.length());
    } else {
        buffer.append((char) _byte);
        if (!methodParsed && buffer.length() > HttpMethod.MAX_LENGTH) {
            throw new HttpParsingException(HttpStatusCode.SERVER_ERROR_501_NOT_IMPLEMENTED);
        }
    }
}
```

**Error handling added:**
- Empty request line (missing method or target) → `400 Bad Request`
- Extra spaces in request line → `400 Bad Request`
- Method name exceeds `MAX_LENGTH` → `501 Not Implemented`
- Unknown HTTP method → `501 Not Implemented` (via `HttpRequest.setMethod()`)

> **Learning point:** HTTP uses CRLF (`\r\n`, bytes 13 + 10) as the line delimiter. The constants `SP = 0x20` (space), `CR = 0x0D`, `LF = 0x0A` are defined for clarity.

> **What's missing:** The parser still needs to (a) extract the version from the request line, (b) parse headers into key-value pairs, and (c) read the body based on `Content-Length`.

### `HttpParsingException.java`

```java
public class HttpParsingException extends Exception {
    private final HttpStatusCode errorCode;

    public HttpParsingException(HttpStatusCode errorCode) {
        super(errorCode.MESSAGE);
        this.errorCode = errorCode;
    }

    public HttpStatusCode getErrorCode() {
        return errorCode;
    }
}
```

A checked exception that carries the appropriate HTTP status code, so error handlers can return the right HTTP error response. The exception message is set to the status code's `MESSAGE` string (e.g., `"Bad Request"`, `"Not Implemented"`).

---

## Utility Layer

### `JxUtil.java` — Jackson JSON Facade

Wraps Jackson's `ObjectMapper` with a clean static API.

| Method | Purpose |
|---|---|
| `parse(String)` | Parse JSON string → `JsonNode` |
| `fromJson(JsonNode, Class)` | Convert `JsonNode` → typed Java object |
| `toJson(Object)` | Convert Java object → `JsonNode` |
| `stringify(JsonNode)` | `JsonNode` → compact JSON string |
| `stringifyPretty(JsonNode)` | `JsonNode` → pretty-printed JSON string |

**Configuration:** `FAIL_ON_UNKNOWN_PROPERTIES = false` — unknown JSON fields are silently ignored, making deserialization lenient.

---

## Build & Run

**Prerequisites:** Java 8+ and Maven.

```bash
# Build the shaded (fat) JAR
mvn clean package

# Run the server
java -jar target/jttp-1.0-SNAPSHOT.jar

# Or run directly from Maven
mvn exec:java -Dexec.mainClass="com.imanimen.jttpserver.JttpServer"
```

**Dependencies** (from `pom.xml`):
| Dependency | Purpose |
|---|---|
| Jackson (annotations + databind) 2.19.1 | JSON parsing |
| SLF4J 2.0.18 + Logback 1.5.34 | Logging |
| JUnit Jupiter 6.0.3 | Testing |

**Maven Shade Plugin** packages everything into a single executable fat JAR.

### CI/CD (GitHub Actions)

`.github/workflows/maven.yml` runs on push/PR to `main`:
1. Sets up JDK 25 (Temurin).
2. Builds with `mvn -B package`.
3. Runs tests explicitly with `mvn -B test` (makes test results visible in CI logs).

---

## Testing

```bash
mvn clean test
```

### `HttpParserTest.java`

Two tests covering valid and invalid requests:

1. **`parseHttpRequest`** — Sends a real Chrome browser request (`GET / HTTP/1.1` with full headers). Asserts the parsed method equals `HttpMethod.GET`.

2. **`parseHttpBadRequest`** — Sends a request with an unsupported method (`GETTT / HTTP/1.1`). Asserts that `HttpParsingException` is thrown with error code `501 NOT_IMPLEMENTED`.

```java
@Test
void parseHttpBadRequest() {
    try {
        HttpRequest request = httpParser.parseHttpRequest(
                generateInValidGETTestCase()  // "GETTT / HTTP/1.1\r\n..."
        );
        fail();  // should not reach here
    } catch (HttpParsingException e) {
        assertEquals(HttpStatusCode.SERVER_ERROR_501_NOT_IMPLEMENTED, e.getErrorCode());
    }
}
```

> **Learning point:** The test constructs the exact byte sequence a real browser would send, making it a good integration-level test. The invalid test uses an unsupported method to verify the parser correctly rejects it.

---

## Current State & Next Steps

### Working ✅
- Server starts and listens on port 8080.
- Accepts connections and returns a hardcoded HTML page.
- Configuration is loaded from `jttp.json`.
- Request line parsing extracts method and target.
- Method validation against `HttpMethod` enum (rejects unknown methods with 501).
- Error handling for malformed request lines (empty, too many spaces, oversized method).
- Unit tests for valid and invalid HTTP requests.
- CI pipeline runs build + tests on push/PR to `main`.

### Not Yet Implemented ❌
| Feature | Where |
|---|---|
| Extract HTTP version from request line | `HttpParser.parseRequestLine()` |
| Parse HTTP headers | `HttpParser.parseHeaders()` |
| Parse HTTP body | `HttpParser.parseBody()` |
| Serve files from webroot | `JttpConnectionWorkerThread` |
| Error responses (404, 500, etc.) | Not yet wired |
| `HttpResponse` class | Not created yet |

### Suggested Implementation Order
1. Extract version from request line (currently parsed but not stored).
2. Implement `parseHeaders()` — parse key-value header pairs.
3. Implement `parseBody()` — read body using `Content-Length`.
4. Wire `HttpParser` into `JttpConnectionWorkerThread`.
5. Implement file serving from `webroot` (read file → write response).
6. Add error handling with proper HTTP status codes.
7. Create `HttpResponse` class.
8. Expand test coverage with edge cases.

---

## Key Concepts to Learn

### 1. HTTP Wire Format
Every HTTP message has this structure:
```
<start-line>\r\n
<header>: <value>\r\n
<header>: <value>\r\n
\r\n
<body>
```

- **Request start-line:** `GET /index.html HTTP/1.1`
- **Response start-line:** `HTTP/1.1 200 OK`
- Headers are terminated by an empty line (double `\r\n`).
- Body length is specified by `Content-Length` header.

### 2. Java Socket Programming
- `ServerSocket` — listens on a port, accepts connections.
- `Socket` — represents one TCP connection.
- `InputStream` / `OutputStream` — read/write raw bytes.
- `InputStreamReader` — converts bytes to characters (needed for ASCII/text).

### 3. Thread-per-Connection Model
- A listener thread accepts connections.
- Each connection gets a dedicated worker thread.
- Simple but limited (does not scale to thousands of connections).
- More advanced alternatives: thread pools, NIO, event loops.

### 4. Singleton Pattern
`ConfigurationManager` uses eager singleton — ensures only one config instance exists.

### 5. Jackson for JSON
- `ObjectMapper` — central Jackson class for JSON operations.
- `JsonNode` — tree model for JSON data.
- `treeToValue()` — convert tree model to typed POJO.
- `FAIL_ON_UNKNOWN_PROPERTIES = false` — lenient parsing.

### 6. Maven Shade Plugin
Creates a fat JAR bundling all dependencies, making deployment simple (`java -jar`).

### 7. SLF4J + Logback
- SLF4J is the logging facade.
- Logback is the implementation.
- Logger instances are obtained per-class: `LoggerFactory.getLogger(ClassName.class)`.

---

## Bug to Note

In `HttpStatusCode.java` line 11:
```java
SERVER_ERROR_501_NOT_IMPLEMENTED(500, "Not Implemented");
```

The status code should be `501`, not `500`. This is a copy-paste error from the `500` entry above it.

---

## Quick Reference

| File | Purpose |
|---|---|
| `JttpServer.java` | Entry point, wires everything together |
| `Configuration.java` | Config data model (port, webroot) |
| `ConfigurationManager.java` | Loads and caches JSON config |
| `ServerListenerThread.java` | Accepts TCP connections in a loop |
| `JttpConnectionWorkerThread.java` | Handles one HTTP request/response |
| `HttpParser.java` | Parses raw bytes → structured request |
| `HttpRequest.java` | Request model (method, target, version) |
| `HttpMethod.java` | Enum of supported methods (GET, POST, PUT, DELETE) |
| `HttpStatusCode.java` | HTTP status code enum |
| `HttpParsingException.java` | Checked exception with HTTP status code |
| `JxUtil.java` | Jackson utility facade |
| `jttp.json` | Server configuration file |
| `HttpParserTest.java` | Unit tests for parser |
| `pom.xml` | Maven build with shade plugin |
| `.github/workflows/maven.yml` | CI pipeline (build + test) |

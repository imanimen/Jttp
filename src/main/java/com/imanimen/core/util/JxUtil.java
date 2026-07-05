package com.imanimen.core.util;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import java.io.IOException;

/**
 * JxUtil - JSON Utility Wrapper with Jackson
 * <p>
 * A facade class that simplifies JSON operations using Jackson's ObjectMapper.
 * Provides static methods for parsing, converting, and serializing JSON data.
 * <p>
 * Key Features:
 * - Lenient deserialization (ignores unknown JSON properties)
 * - Type-safe object conversion
 * - Pretty-printing support
 * - Single ObjectMapper instance for efficient reuse
 * <p>
 * Example Usage:
 * <pre>
 * // Parse JSON string
 * JsonNode node = JxUtil.parse("{\"name\": \"John\", \"age\": 30}");
 *
 * // Convert to Java object
 * User user = JxUtil.fromJson(node, User.class);
 *
 * // Convert object to JSON
 * JsonNode jsonNode = JxUtil.toJson(user);
 *
 * // Pretty print
 * System.out.println(JxUtil.stringifyPretty(jsonNode));
 * </pre>
 *
 * @version 1.0
 */
public class JxUtil {

    /** Shared ObjectMapper instance configured with default settings */
    private static ObjectMapper jxObjMapper = jxDefaultObjMapper();

    /**
     * Initializes and configures the default ObjectMapper with lenient deserialization.
     * <p>
     * Configuration Details:
     * - FAIL_ON_UNKNOWN_PROPERTIES is set to false, allowing JSON with extra fields
     *   to be successfully deserialized even if the target Java class doesn't have
     *   those properties. Extra fields are silently ignored.
     *
     * @return Configured ObjectMapper instance ready for JSON processing
     */
    private static ObjectMapper jxDefaultObjMapper() {
        ObjectMapper jxObjMap = new ObjectMapper();
        // makes parsing not crash if property is not available
        jxObjMap.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return jxObjMap;
    }

    /**
     * Parses a JSON string into a JsonNode tree structure.
     * <p>
     * This method reads a JSON string and converts it into an intermediate
     * JsonNode representation, which can be further processed, queried, or
     * converted to Java objects.
     *
     * @param jsonSrc the JSON string to parse (cannot be null)
     * @return a JsonNode representing the parsed JSON structure
     * @throws IOException if the JSON string is malformed or cannot be read
     *
     * @example
     * <pre>
     * String json = "{\"name\": \"Alice\", \"age\": 25}";
     * JsonNode node = JxUtil.parse(json);
     * System.out.println(node.get("name")); // Output: "Alice"
     * </pre>
     */
    public static JsonNode parse(String jsonSrc) throws IOException {
        return jxObjMapper.readTree(jsonSrc);
    }

    /**
     * Converts a JsonNode to a Java object of the specified type.
     * <p>
     * This method is useful for type-safe conversion when you have a JsonNode
     * and want to map it to a strongly-typed Java class. The conversion respects
     * the ObjectMapper's configuration (e.g., ignoring unknown properties).
     *
     * @param <A> the target Java class type
     * @param node the JsonNode to convert (cannot be null)
     * @param clazz the target class to convert the JsonNode into
     * @return an instance of the specified class with values from the JsonNode
     * @throws JsonProcessingException if the node cannot be converted to the target class
     *         (e.g., type mismatch, missing required fields)
     *
     * @example
     * <pre>
     * JsonNode node = JxUtil.parse("{\"id\": 1, \"name\": \"John\"}");
     * User user = JxUtil.fromJson(node, User.class);
     * System.out.println(user.getName()); // Output: "John"
     * </pre>
     */
    public static <A> A fromJson(JsonNode node, Class<A> clazz) throws JsonProcessingException {
        return jxObjMapper.treeToValue(node, clazz);
    }

    /**
     * Converts a Java object to a JsonNode representation.
     * <p>
     * This method serializes a Java object into a JsonNode, which can then be
     * manipulated, queried, or converted back to a string. This is useful for
     * transforming objects between different formats or building JSON dynamically.
     *
     * @param obj the Java object to convert (cannot be null)
     * @return a JsonNode representing the object in JSON format
     *
     * @example
     * <pre>
     * User user = new User(1, "Alice", "alice@example.com");
     * JsonNode node = JxUtil.toJson(user);
     * System.out.println(node.get("name")); // Output: "Alice"
     * </pre>
     */
    public static JsonNode toJson(Object obj) {
        return jxObjMapper.valueToTree(obj);
    }

    /**
     * Converts a JsonNode to a compact JSON string (no extra whitespace).
     * <p>
     * This method serializes a JsonNode into a single-line JSON string without
     * any formatting or indentation. Useful for compact storage, transmission,
     * or logging.
     *
     * @param node the JsonNode to stringify (cannot be null)
     * @return a compact JSON string representation of the node
     * @throws JsonProcessingException if the node cannot be converted to a string
     *
     * @example
     * <pre>
     * JsonNode node = JxUtil.parse("{\"name\": \"Bob\", \"age\": 30}");
     * String json = JxUtil.stringify(node);
     * // Output: {"name":"Bob","age":30}
     * </pre>
     */
    public static String stringify(JsonNode node) throws JsonProcessingException {
        return generate(node, false);
    }

    /**
     * Converts a JsonNode to a formatted, pretty-printed JSON string.
     * <p>
     * This method serializes a JsonNode into a multi-line JSON string with
     * proper indentation and whitespace. Useful for debugging, logging,
     * or displaying JSON to users in a readable format.
     *
     * @param node the JsonNode to stringify (cannot be null)
     * @return a formatted JSON string with indentation and line breaks
     * @throws JsonProcessingException if the node cannot be converted to a string
     *
     * @example
     * <pre>
     * JsonNode node = JxUtil.parse("{\"name\": \"Charlie\", \"age\": 35}");
     * String prettyJson = JxUtil.stringifyPretty(node);
     * // Output:
     * // {
     * //   "name" : "Charlie",
     * //   "age" : 35
     * // }
     * </pre>
     */
    public static String stringifyPretty(JsonNode node) throws JsonProcessingException {
        return generate(node, true);
    }

    /**
     * Internal helper method that generates JSON string with optional formatting.
     * <p>
     * This is a private utility method used by stringify() and stringifyPretty()
     * to consolidate the JSON generation logic. It handles both compact and
     * formatted output based on the 'pretty' parameter.
     *
     * @param object the object to convert to JSON string (can be JsonNode or other objects)
     * @param pretty if true, applies indentation for readable output;
     *               if false, generates compact single-line output
     * @return a JSON string representation of the object
     * @throws JsonProcessingException if the object cannot be converted to a string
     */
    private static String generate(Object object, boolean pretty) throws JsonProcessingException {
        ObjectWriter objectWriter = jxDefaultObjMapper().writer();
        if (pretty) {
            objectWriter = objectWriter.with(SerializationFeature.INDENT_OUTPUT);
        }
        return objectWriter.writeValueAsString(object);
    }
}
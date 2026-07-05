package com.imanimen.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;

import java.io.IOException;

/**
 * Driver JxUtil -> Json util with Jackson Json handler
 */
public class JxUtil {

    private static ObjectMapper jxObjMapper = jxDefaultObjMapper();

    private static ObjectMapper jxDefaultObjMapper() {
        ObjectMapper jxObjMap = new ObjectMapper();
        // makes parsing not crash if property is not available
        jxObjMap.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return jxObjMap;
    }

    public static JsonNode parse(String jsonSrc) throws IOException {
        return jxObjMapper.readTree(jsonSrc);
    }

    public static <A> A fromJson(JsonNode node, Class<A> clazz) throws JsonProcessingException {
        return jxObjMapper.treeToValue(node, clazz);
    }

    public static JsonNode toJson(Object obj) {
        return jxObjMapper.valueToTree(obj);
    }

    public static String stringify(JsonNode node) throws JsonProcessingException {
        return generate(node, false);
    }

    public static String stringifyPretty(JsonNode node) throws JsonProcessingException {
        return generate(node, true
        );
    }

    private static String generate(Object object, boolean pretty) throws JsonProcessingException {
        ObjectWriter objectWriter = jxDefaultObjMapper().writer();
        if (pretty) {
            objectWriter = objectWriter.with(SerializationFeature.INDENT_OUTPUT);
        }
        return objectWriter.writeValueAsString(object);
    }

}

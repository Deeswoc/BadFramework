package utils.VeryBadHTTP.BodyParser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;

import java.io.IOException;

public class Json {
    private static ObjectMapper objectMapper = getDefaultObjectMapper();

    private static ObjectMapper getDefaultObjectMapper() {
        ObjectMapper defaultObjectMapper = new ObjectMapper();
        defaultObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return defaultObjectMapper;
    }

    public static JsonNode parse(String src) throws IOException {
        return objectMapper.readTree(src);
    }

    public static <A> A fromJson(JsonNode node, Class<A> aClass) throws JsonProcessingException {
        return objectMapper.treeToValue(node, aClass);
    }

    public static String stringify(JsonNode node) throws JsonProcessingException {
        ObjectWriter objectWriter = objectMapper.writer();
        return objectWriter.writeValueAsString(node);
    }

    private static String prettyPrint(JsonNode node) throws JsonProcessingException {
        ObjectWriter objectWriter = objectMapper.writer().with(SerializationFeature.INDENT_OUTPUT);
        return objectWriter.writeValueAsString(node);
    }

    public static String stringify(JsonNode node, boolean pretty) throws JsonProcessingException {
        if (pretty) {
            return prettyPrint(node);
        } else {
            return stringify(node);
        }
    }

    public static JsonNode toJson(Object object) {
        return objectMapper.valueToTree(object);
    }
}

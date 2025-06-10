import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Custom Gson adapter for LocalDateTime serialization
 * Ensures proper ISO-8601 formatting in JSON
 */
public class LocalDateTimeAdapter {
    implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        private static final DateTimeFormatter FORMATTER =
                DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    }

    /**
     * Serializes LocalDateTime to JSON String
     */
    @Override
    public JsonElement serialize(
            LocalDateTime date, Type type, JsonSerializationContext context
    ) {
        return new JsonPrimitive(date.format(FORMATTER));
    }

    /**
     * Deserialize JSON string to LocalDateTime
     */
    @Override
    public LocalDateTime deserialize(
            JsonElement json, Type type, JsonDeserializationContext context
    ) throws JsonParseException {
        return LocalDateTime.parse(json.getAsString(), FORMATTER);
    }
}
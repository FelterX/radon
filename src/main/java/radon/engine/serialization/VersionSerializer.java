package radon.engine.serialization;

import com.google.gson.*;
import radon.engine.util.Version;

import java.lang.reflect.Type;

public class VersionSerializer implements JsonSerializer<Version>, JsonDeserializer<Version> {
    @Override
    public JsonElement serialize(Version version, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("major", version.major());
        jsonObject.addProperty("minor", version.minor());
        jsonObject.addProperty("revision", version.revision());

        return jsonObject;
    }

    @Override
    public Version deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = (JsonObject) json;

        int major = jsonObject.get("major").getAsInt();
        int minor = jsonObject.get("minor").getAsInt();
        int revision = jsonObject.get("revision").getAsInt();

        return new Version(major, minor, revision);
    }
}

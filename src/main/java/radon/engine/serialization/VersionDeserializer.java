package radon.engine.serialization;

import com.google.gson.*;
import radon.engine.util.Version;

import java.lang.reflect.Type;

public class VersionDeserializer implements JsonDeserializer<Version> {


    @Override
    public Version deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = (JsonObject) json;

        int major = jsonObject.get("major").getAsInt();
        int minor = jsonObject.get("minor").getAsInt();
        int revision = jsonObject.get("revision").getAsInt();

        return new Version(major, minor, revision);
    }
}

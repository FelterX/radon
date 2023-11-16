package radon.engine.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import radon.engine.util.Version;

import java.lang.reflect.Type;

public class VersionSerializer implements JsonSerializer<Version> {
    @Override
    public JsonElement serialize(Version version, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("major", version.major());
        jsonObject.addProperty("minor", version.minor());
        jsonObject.addProperty("revision", version.revision());

        return jsonObject;
    }
}

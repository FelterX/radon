package radon.project.serialization;

import com.google.gson.*;
import radon.engine.util.Version;
import radon.project.GameType;
import radon.project.Project;

import java.lang.reflect.Type;

public class ProjectDeserializer implements JsonDeserializer<Project> {
    @Override
    public Project deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = (JsonObject) json;
        String name = jsonObject.get("name").getAsString();
        String path = jsonObject.get("path").getAsString();
        Version version = context.deserialize(jsonObject.get("version"), Version.class);
        GameType gameType = context.deserialize(jsonObject.get("gameType"), GameType.class);
        return new Project(name, path, version, gameType);
    }
}

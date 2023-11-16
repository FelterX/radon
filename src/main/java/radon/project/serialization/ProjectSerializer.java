package radon.project.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import radon.project.Project;

import java.lang.reflect.Type;

public class ProjectSerializer implements JsonSerializer<Project> {
    @Override
    public JsonElement serialize(Project project, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", project.getName());
        jsonObject.addProperty("path", project.getPath());
        jsonObject.add("version", context.serialize(project.getVersion()));
        jsonObject.add("gameType", context.serialize(project.getGameType()));

        return jsonObject;
    }
}

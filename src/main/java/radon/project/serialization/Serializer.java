package radon.project.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import radon.engine.serialization.VersionSerializer;
import radon.engine.util.Version;
import radon.project.Project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Serializer {
    private static final Gson SERIALIZER;

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Version.class, new VersionSerializer());
        builder.registerTypeAdapter(Project.class, new ProjectSerializer());
        builder.registerTypeAdapter(Project.class, new ProjectDeserializer());

        SERIALIZER = builder.create();
    }

    public static Gson getSerializer() {
        return SERIALIZER;
    }

    public static Project deserialize(String filePath) {
        try {
            String jsonSrc = Files.readString(Paths.get(filePath));
            return SERIALIZER.fromJson(jsonSrc, Project.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

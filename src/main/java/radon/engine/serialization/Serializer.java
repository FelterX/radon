package radon.engine.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Serializer {
    private static final Gson SERIALIZER;
    static {
        GsonBuilder builder = new GsonBuilder();
        SERIALIZER = builder.create();
    }

    public static Gson getSerializer(){
        return SERIALIZER;
    }
}

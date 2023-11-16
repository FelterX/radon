package radon.engine.core;

import radon.engine.util.types.Singleton;

import static org.lwjgl.glfw.GLFW.*;

public final class Time extends RadonSystem {

    public static final float IDEAL_DELTA_TIME = 1.0f / 60.0f;

    @Singleton
    private static Time instance;

    public static float nano() {
        return (float) (seconds() * 1e9);
    }

    public static float millis() {
        return seconds() * 1000.0f;
    }

    public static float seconds() {
        return time();
    }

    public static float minutes() {
        return time() / 60.0f;
    }

    public static float hours() {
        return time() / 3600.0f;
    }

    public static float time() {
        return (float)glfwGetTime();
    }

    public static String format() {
        return String.format("%02d:%02d:%02d", (int) hours(), (int) minutes() % 60, (int) seconds() % 60);
    }

    public static float deltaTime() {
        return instance.deltaTime;
    }

    public static float fps() {
        return instance.fps;
    }

    public static float ups() {
        return instance.ups;
    }

    public static long frames() {
        return instance.frames;
    }

    public static float frequency() {
        return (float)glfwGetTimerFrequency();
    }

    float deltaTime;
    float fps;
    float ups;
    long frames;

    private Time(RadonSystemManager systemManager) {
        super(systemManager);
    }

    @Override
    protected void init() {
        glfwSetTime(RadonConfigConstants.INITIAL_TIME_VALUE);
    }

    @Override
    protected void terminate() {

    }

}

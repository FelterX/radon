package radon.engine.core;

import org.joml.Vector2ic;
import radon.engine.graphics.GraphicsAPI;
import radon.engine.graphics.rendering.ShadingModel;
import radon.engine.graphics.window.CursorType;
import radon.engine.graphics.window.DisplayMode;
import radon.engine.logging.Log;
import radon.engine.logging.LogChannel;
import radon.engine.util.ANSIColor;
import radon.engine.util.Version;
import radon.engine.util.geometry.Sizec;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public final class RadonConfiguration<T> {

    static {
        DefaultRadonConfigurations.ensureDefaultConfigurationsClassIsLoaded();
    }

    public static final RadonConfiguration<Boolean> DEBUG = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_DEBUG);

    public static final RadonConfiguration<Boolean> INTERNAL_DEBUG = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_INTERNAL_DEBUG);

    public static final RadonConfiguration<Boolean> SHOW_DEBUG_INFO = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_SHOW_DEBUG_INFO);

    public static final RadonConfiguration<Boolean> FAST_MATH = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_FAST_MATH);

    public static final RadonConfiguration<Boolean> MULTISAMPLE_ENABLE = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_MULTISAMPLE_ENABLE);

    public static final RadonConfiguration<Integer> MSAA_SAMPLES = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_MSAA_SAMPLES);

    public static final RadonConfiguration<Boolean> MEMORY_USAGE_REPORT = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_MEMORY_USAGE_REPORT);

    public static final RadonConfiguration<Boolean> EVENTS_DEBUG_REPORT = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_EVENTS_DEBUG_REPORT);

    public static final RadonConfiguration<Boolean> SCENES_DEBUG_REPORT = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_SCENES_DEBUG_REPORT);

    public static final RadonConfiguration<String> APPLICATION_NAME = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_APPLICATION_NAME);

    public static final RadonConfiguration<Version> APPLICATION_VERSION = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_APPLICATION_VERSION);

    public static final RadonConfiguration<Double> INITIAL_TIME_VALUE = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_INITIAL_TIME_VALUE);

    public static final RadonConfiguration<Set<Log.Level>> LOG_LEVELS = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_LOG_LEVELS);

    public static final RadonConfiguration<Map<Log.Level, ANSIColor>> LOG_LEVEL_COLORS = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_LOG_LEVEL_COLORS);

    public static final RadonConfiguration<Collection<LogChannel>> LOG_CHANNELS = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_LOG_CHANNELS);

    public static final RadonConfiguration<DateTimeFormatter> LOG_DATETIME_FORMATTER = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_LOG_DATETIME_FORMATTER);

    public static final RadonConfiguration<Boolean> ENABLE_ASSERTS = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_ENABLE_ASSERTS);

    public static final RadonConfiguration<GraphicsAPI> GRAPHICS_API = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_GRAPHICS_API);

    public static final RadonConfiguration<ShadingModel> SCENE_SHADING_MODEL = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_SCENE_SHADING_MODEL);

    public static final RadonConfiguration<Boolean> SHADOWS_ENABLED_ON_START = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_SHADOWS_ENABLED_ON_START);

    public static final RadonConfiguration<Boolean> SHOW_DEBUG_INFO_ON_WINDOW_TITLE = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_SHOW_DEBUG_INFO_ON_WINDOW_TITLE);

    public static final RadonConfiguration<Vector2ic> WINDOW_POSITION = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_WINDOW_POSITION);

    public static final RadonConfiguration<Sizec> WINDOW_SIZE = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_WINDOW_SIZE);

    public static final RadonConfiguration<DisplayMode> WINDOW_DISPLAY_MODE = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_WINDOW_DISPLAY_MODE);

    public static final RadonConfiguration<CursorType> WINDOW_CURSOR_TYPE = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_WINDOW_CURSOR_TYPE);

    public static final RadonConfiguration<Boolean> WINDOW_VISIBLE = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_WINDOW_VISIBLE);

    public static final RadonConfiguration<Boolean> WINDOW_RESIZABLE = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_WINDOW_RESIZABLE);

    public static final RadonConfiguration<Boolean> WINDOW_FOCUS_ON_SHOW = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_WINDOW_FOCUS_ON_SHOW);

    public static final RadonConfiguration<Boolean> VSYNC = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_VSYNC);

    public static final RadonConfiguration<Boolean> OPENGL_ENABLE_DEBUG_MESSAGES = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_OPENGL_ENABLE_DEBUG_MESSAGES);

    public static final RadonConfiguration<Boolean> OPENGL_ENABLE_WARNINGS_UNIFORMS = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_OPENGL_ENABLE_WARNINGS_UNIFORMS);

    public static final RadonConfiguration<Boolean> PRINT_SHADERS_SOURCE = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_PRINT_SHADERS_SOURCE);

    public static final RadonConfiguration<Boolean> GRAPHICS_MULTITHREADING_ENABLED = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_GRAPHICS_MULTITHREADING_ENABLED);

    public static final RadonConfiguration<String> FIRST_SCENE_NAME = new RadonConfiguration<>(DefaultRadonConfigurations.DEFAULT_FIRST_SCENE_NAME);

    static void ensureLoaded() {
    }

    private T value;

    private RadonConfiguration(T defaultValue) {
        value = defaultValue;
    }
    
    public void set(T value) {
        if (Radon.LAUNCHED.get()) {
            Log.warning("Setting configuration values after Radon has been launched has no effect");
        } else {
            this.value = value;
        }
    }

    public T get() {
        return (T) value;
    }
}

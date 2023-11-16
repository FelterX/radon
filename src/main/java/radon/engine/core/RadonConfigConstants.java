package radon.engine.core;

import org.joml.Vector2ic;
import org.lwjgl.system.Configuration;
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

public final class RadonConfigConstants {


    static {
        RadonConfiguration.ensureLoaded();
    }

    public static final boolean DEBUG = RadonConfiguration.DEBUG.get();

    public static final boolean INTERNAL_DEBUG = RadonConfiguration.INTERNAL_DEBUG.get();

    public static final boolean SHOW_DEBUG_INFO = RadonConfiguration.SHOW_DEBUG_INFO.get();

    public static final boolean FAST_MATH = RadonConfiguration.FAST_MATH.get();

    public static final boolean MULTISAMPLE_ENABLE = RadonConfiguration.MULTISAMPLE_ENABLE.get();

    public static final int MSAA_SAMPLES = RadonConfiguration.MSAA_SAMPLES.get();

    public static final boolean MEMORY_USAGE_REPORT = RadonConfiguration.MEMORY_USAGE_REPORT.get();

    public static final boolean EVENTS_DEBUG_REPORT = RadonConfiguration.EVENTS_DEBUG_REPORT.get();

    public static final boolean SCENES_DEBUG_REPORT = RadonConfiguration.SCENES_DEBUG_REPORT.get();

    public static final String APPLICATION_NAME = RadonConfiguration.APPLICATION_NAME.get();

    public static final Version APPLICATION_VERSION = RadonConfiguration.APPLICATION_VERSION.get();

    public static final double INITIAL_TIME_VALUE = RadonConfiguration.INITIAL_TIME_VALUE.get();

    public static final Set<Log.Level> LOG_LEVELS = RadonConfiguration.LOG_LEVELS.get();

    public static final Map<Log.Level, ANSIColor> LOG_LEVEL_COLORS = RadonConfiguration.LOG_LEVEL_COLORS.get();

    public static final Collection<LogChannel> LOG_CHANNELS = RadonConfiguration.LOG_CHANNELS.get();

    public static final DateTimeFormatter LOG_DATETIME_FORMATTER = RadonConfiguration.LOG_DATETIME_FORMATTER.get();

    public static final boolean ENABLE_ASSERTS = RadonConfiguration.ENABLE_ASSERTS.get();

    public static final GraphicsAPI GRAPHICS_API = RadonConfiguration.GRAPHICS_API.get();

    public static final ShadingModel SCENE_SHADING_MODEL = RadonConfiguration.SCENE_SHADING_MODEL.get();

    public static final boolean SHADOWS_ENABLED_ON_START = RadonConfiguration.SHADOWS_ENABLED_ON_START.get();

    public static final boolean SHOW_DEBUG_INFO_ON_WINDOW_TITLE = RadonConfiguration.SHOW_DEBUG_INFO_ON_WINDOW_TITLE.get();

    public static final Vector2ic WINDOW_POSITION = RadonConfiguration.WINDOW_POSITION.get();

    public static final Sizec WINDOW_SIZE = RadonConfiguration.WINDOW_SIZE.get();

    public static final DisplayMode WINDOW_DISPLAY_MODE = RadonConfiguration.WINDOW_DISPLAY_MODE.get();

    public static final CursorType WINDOW_CURSOR_TYPE = RadonConfiguration.WINDOW_CURSOR_TYPE.get();

    public static final boolean WINDOW_VISIBLE = RadonConfiguration.WINDOW_VISIBLE.get();

    public static final boolean WINDOW_RESIZABLE = RadonConfiguration.WINDOW_RESIZABLE.get();

    public static final boolean WINDOW_FOCUS_ON_SHOW = RadonConfiguration.WINDOW_FOCUS_ON_SHOW.get();

    public static final boolean VSYNC = RadonConfiguration.VSYNC.get();

    public static final boolean OPENGL_ENABLE_DEBUG_MESSAGES = RadonConfiguration.OPENGL_ENABLE_DEBUG_MESSAGES.get();

    public static final boolean OPENGL_ENABLE_WARNINGS_UNIFORMS = RadonConfiguration.OPENGL_ENABLE_WARNINGS_UNIFORMS.get();

    public static final boolean PRINT_SHADERS_SOURCE = RadonConfiguration.PRINT_SHADERS_SOURCE.get();

    public static final boolean GRAPHICS_MULTITHREADING_ENABLED = RadonConfiguration.GRAPHICS_MULTITHREADING_ENABLED.get();

    public static final String FIRST_SCENE_NAME = RadonConfiguration.FIRST_SCENE_NAME.get();

    static void ensureLoaded() {
        setLWJGLConfiguration();
        setJOMLConfiguration();
    }

    private static void setLWJGLConfiguration() {
        Configuration.DEBUG_STREAM.set(new LWJGLDebugStream());
        Configuration.DEBUG.set(INTERNAL_DEBUG);
        // Configuration.DEBUG_STACK.set(INTERNAL_DEBUG);
        Configuration.DEBUG_MEMORY_ALLOCATOR_INTERNAL.set(INTERNAL_DEBUG);
        Configuration.DEBUG_MEMORY_ALLOCATOR.set(INTERNAL_DEBUG);
        Configuration.DEBUG_LOADER.set(INTERNAL_DEBUG);
        Configuration.DEBUG_FUNCTIONS.set(INTERNAL_DEBUG);
        Configuration.GLFW_CHECK_THREAD0.set(INTERNAL_DEBUG);
        Configuration.DISABLE_CHECKS.set(!INTERNAL_DEBUG);
        Configuration.DISABLE_FUNCTION_CHECKS.set(!INTERNAL_DEBUG);
    }

    private static void setJOMLConfiguration() {
        System.setProperty("joml.debug", String.valueOf(INTERNAL_DEBUG));
        System.setProperty("joml.fastmath", String.valueOf(FAST_MATH));
        System.setProperty("joml.sinLookup", String.valueOf(FAST_MATH));
        System.setProperty("joml.format", String.valueOf(false));
    }

    private RadonConfigConstants() {}
}

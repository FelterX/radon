package radon.engine.core;

import org.joml.Vector2ic;
import radon.engine.graphics.GraphicsAPI;
import radon.engine.graphics.rendering.ShadingModel;
import radon.engine.graphics.window.CursorType;
import radon.engine.graphics.window.DisplayMode;
import radon.engine.logging.Log;
import radon.engine.logging.LogChannel;
import radon.engine.logging.LogFileChannel;
import radon.engine.util.ANSIColor;
import radon.engine.util.Version;
import radon.engine.util.geometry.Sizec;

import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.*;

class DefaultRadonConfigurations {


    static final boolean DEFAULT_DEBUG = true;

    static final boolean DEFAULT_INTERNAL_DEBUG = false;

    static final boolean DEFAULT_SHOW_DEBUG_INFO = true;

    static final boolean DEFAULT_FAST_MATH = true;

    static final boolean DEFAULT_MULTISAMPLE_ENABLE = true;

    static final int DEFAULT_MSAA_SAMPLES = 4;

    static final boolean DEFAULT_MEMORY_USAGE_REPORT = false;

    static final boolean DEFAULT_EVENTS_DEBUG_REPORT = false;

    static final boolean DEFAULT_SCENES_DEBUG_REPORT = false;

    static final String DEFAULT_APPLICATION_NAME = "Unnamed Radon Application";

    static final Version DEFAULT_APPLICATION_VERSION = new Version(0, 0, 1);

    static final double DEFAULT_INITIAL_TIME_VALUE = 0.0;

    static final Set<Log.Level> DEFAULT_LOG_LEVELS = EnumSet.allOf(Log.Level.class);

    static final Map<Log.Level, ANSIColor> DEFAULT_LOG_LEVEL_COLORS = new EnumMap<>(Log.Level.class);
    static {
        DEFAULT_LOG_LEVEL_COLORS.put(Log.Level.TRACE, ANSIColor.NONE);
        DEFAULT_LOG_LEVEL_COLORS.put(Log.Level.INFO, ANSIColor.BLUE);
        DEFAULT_LOG_LEVEL_COLORS.put(Log.Level.DEBUG, ANSIColor.GREEN);
        DEFAULT_LOG_LEVEL_COLORS.put(Log.Level.LWJGL, ANSIColor.MAGENTA);
        DEFAULT_LOG_LEVEL_COLORS.put(Log.Level.WARNING, ANSIColor.YELLOW);
        DEFAULT_LOG_LEVEL_COLORS.put(Log.Level.ERROR, ANSIColor.RED);
        DEFAULT_LOG_LEVEL_COLORS.put(Log.Level.FATAL, ANSIColor.RED_BOLD);
    }

    static final Collection<LogChannel> DEFAULT_LOG_CHANNELS = Arrays.asList(
            LogChannel.stdout(),
            new LogFileChannel(Paths.get("radon.log"), Log.Level.ERROR, StandardOpenOption.CREATE));

    static final DateTimeFormatter DEFAULT_LOG_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/YYYY HH:mm:ss");

    static final boolean DEFAULT_ENABLE_ASSERTS = true;

    static final GraphicsAPI DEFAULT_GRAPHICS_API = GraphicsAPI.OPENGL;

    static final ShadingModel DEFAULT_SCENE_SHADING_MODEL = ShadingModel.PHONG;

    static final boolean DEFAULT_SHADOWS_ENABLED_ON_START = true;

    static final boolean DEFAULT_SHOW_DEBUG_INFO_ON_WINDOW_TITLE = true;

    static final Vector2ic DEFAULT_WINDOW_POSITION = null;

    static final Sizec DEFAULT_WINDOW_SIZE = null;

    static final DisplayMode DEFAULT_WINDOW_DISPLAY_MODE = DisplayMode.WINDOWED;

    static final CursorType DEFAULT_WINDOW_CURSOR_TYPE = CursorType.NORMAL;

    static final boolean DEFAULT_WINDOW_VISIBLE = true;

    static final boolean DEFAULT_WINDOW_RESIZABLE = true;

    static final boolean DEFAULT_WINDOW_FOCUS_ON_SHOW = true;

    static final boolean DEFAULT_VSYNC = true;

    static final boolean DEFAULT_OPENGL_ENABLE_DEBUG_MESSAGES = true;

    static final boolean DEFAULT_OPENGL_ENABLE_WARNINGS_UNIFORMS = false;

    static final boolean DEFAULT_PRINT_SHADERS_SOURCE = false;

    static final boolean DEFAULT_GRAPHICS_MULTITHREADING_ENABLED = true;

    static final String DEFAULT_FIRST_SCENE_NAME = "__Unnamed Scene";

    static void ensureDefaultConfigurationsClassIsLoaded() {
    }


}

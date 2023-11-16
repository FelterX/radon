package radon.engine.core;

public final class RadonConfigurationHelper {

    public static void debugConfiguration() {
        RadonConfiguration.INTERNAL_DEBUG.set(true);
        RadonConfiguration.DEBUG.set(true);
        RadonConfiguration.SHOW_DEBUG_INFO.set(true);
        RadonConfiguration.MEMORY_USAGE_REPORT.set(true);
        RadonConfiguration.FAST_MATH.set(false);
        RadonConfiguration.ENABLE_ASSERTS.set(true);
        RadonConfiguration.SCENES_DEBUG_REPORT.set(true);
        RadonConfiguration.EVENTS_DEBUG_REPORT.set(true);
        RadonConfiguration.OPENGL_ENABLE_DEBUG_MESSAGES.set(true);
        RadonConfiguration.SHOW_DEBUG_INFO_ON_WINDOW_TITLE.set(true);
        RadonConfiguration.OPENGL_ENABLE_WARNINGS_UNIFORMS.set(true);
        RadonConfiguration.PRINT_SHADERS_SOURCE.set(true);
        RadonConfiguration.VSYNC.set(false);
    }

    public static void developmentConfiguration() {
        RadonConfiguration.INTERNAL_DEBUG.set(true);
        RadonConfiguration.DEBUG.set(true);
        RadonConfiguration.SHOW_DEBUG_INFO.set(true);
        RadonConfiguration.MEMORY_USAGE_REPORT.set(true);
        RadonConfiguration.FAST_MATH.set(true);
        RadonConfiguration.ENABLE_ASSERTS.set(true);
        RadonConfiguration.SCENES_DEBUG_REPORT.set(false);
        RadonConfiguration.EVENTS_DEBUG_REPORT.set(false);
        RadonConfiguration.OPENGL_ENABLE_DEBUG_MESSAGES.set(true);
        RadonConfiguration.SHOW_DEBUG_INFO_ON_WINDOW_TITLE.set(true);
        RadonConfiguration.OPENGL_ENABLE_WARNINGS_UNIFORMS.set(false);
        RadonConfiguration.PRINT_SHADERS_SOURCE.set(false);
        RadonConfiguration.VSYNC.set(false);
    }

    public static void debugReleaseConfiguration() {
        RadonConfiguration.INTERNAL_DEBUG.set(false);
        RadonConfiguration.DEBUG.set(false);
        RadonConfiguration.SHOW_DEBUG_INFO.set(true);
        RadonConfiguration.MEMORY_USAGE_REPORT.set(false);
        RadonConfiguration.FAST_MATH.set(false);
        RadonConfiguration.ENABLE_ASSERTS.set(false);
        RadonConfiguration.SCENES_DEBUG_REPORT.set(false);
        RadonConfiguration.EVENTS_DEBUG_REPORT.set(false);
        RadonConfiguration.OPENGL_ENABLE_DEBUG_MESSAGES.set(true);
        RadonConfiguration.SHOW_DEBUG_INFO_ON_WINDOW_TITLE.set(true);
        RadonConfiguration.OPENGL_ENABLE_WARNINGS_UNIFORMS.set(false);
        RadonConfiguration.PRINT_SHADERS_SOURCE.set(false);
        RadonConfiguration.VSYNC.set(false);
    }

    public static void releaseConfiguration() {
        RadonConfiguration.INTERNAL_DEBUG.set(false);
        RadonConfiguration.DEBUG.set(false);
        RadonConfiguration.SHOW_DEBUG_INFO.set(false);
        RadonConfiguration.MEMORY_USAGE_REPORT.set(false);
        RadonConfiguration.FAST_MATH.set(true);
        RadonConfiguration.ENABLE_ASSERTS.set(false);
        RadonConfiguration.SCENES_DEBUG_REPORT.set(false);
        RadonConfiguration.EVENTS_DEBUG_REPORT.set(false);
        RadonConfiguration.OPENGL_ENABLE_DEBUG_MESSAGES.set(false);
        RadonConfiguration.SHOW_DEBUG_INFO_ON_WINDOW_TITLE.set(false);
        RadonConfiguration.OPENGL_ENABLE_WARNINGS_UNIFORMS.set(false);
        RadonConfiguration.PRINT_SHADERS_SOURCE.set(false);
        RadonConfiguration.VSYNC.set(true);
    }

    private RadonConfigurationHelper() {}
}

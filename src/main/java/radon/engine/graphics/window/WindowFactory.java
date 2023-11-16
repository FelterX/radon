package radon.engine.graphics.window;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.Platform;
import radon.engine.graphics.GraphicsAPI;
import radon.engine.util.geometry.Size;
import radon.engine.util.geometry.Sizec;

import static org.lwjgl.glfw.GLFW.*;
import static radon.engine.core.RadonConfigConstants.*;
import static radon.engine.util.Asserts.assertNonNull;
import static radon.engine.util.Asserts.assertNotEquals;
import static radon.engine.util.handles.LongHandle.NULL;
import static radon.engine.util.types.TypeUtils.initSingleton;

public final class WindowFactory {

    private static final int DEFAULT_WIDTH = 1280;
    private static final int DEFAULT_HEIGHT = 720;

    private WindowFactory() {}

    public Window newWindow() {

        setWindowHints();

        String title = APPLICATION_NAME;
        DisplayMode displayMode = WINDOW_DISPLAY_MODE;
        Sizec defaultSize = WINDOW_SIZE == null ? new Size(DEFAULT_WIDTH, DEFAULT_HEIGHT) : WINDOW_SIZE;

        Window window = new Window(createGLFWHandle(title, displayMode, defaultSize), title, displayMode, defaultSize);

        initSingleton(Window.class, window);

        return window;
    }

    private void setWindowHints() {

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

        glfwWindowHint(GLFW_RESIZABLE, asGLFWBoolean(WINDOW_RESIZABLE));
        glfwWindowHint(GLFW_FOCUS_ON_SHOW, asGLFWBoolean(WINDOW_FOCUS_ON_SHOW));

        setGraphicsAPIDependentWindowHints();
    }

    private void setGraphicsAPIDependentWindowHints() {

        switch(GraphicsAPI.get()) {
            case OPENGL:
                setOpenGLWindowHints();
                break;
        }
    }


    private void setOpenGLWindowHints() {

        glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_API);

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, GraphicsAPI.OPENGL.versionMajor());
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, GraphicsAPI.OPENGL.versionMinor());

        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        if(Platform.get() == Platform.MACOSX) {
            glfwWindowHint(GLFW_OPENGL_COMPAT_PROFILE, GLFW_TRUE);
        }

        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, DEBUG ? GLFW_TRUE : GLFW_FALSE);

        glfwWindowHint(GLFW_SRGB_CAPABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_TRUE);

        if(MULTISAMPLE_ENABLE) {
            glfwWindowHint(GLFW_SAMPLES, MSAA_SAMPLES);
        }
    }

    private int asGLFWBoolean(boolean value) {
        return value ? GLFW_TRUE : GLFW_FALSE;
    }

    private long createGLFWHandle(String title, DisplayMode displayMode, Sizec size) {

        int width = size.width();
        int height = size.height();

        long monitor = glfwGetPrimaryMonitor();

        if(displayMode != DisplayMode.WINDOWED) {
            GLFWVidMode vidMode = assertNonNull(glfwGetVideoMode(monitor));
            width = vidMode.width();
            height = vidMode.height();
        }

        if(displayMode != DisplayMode.FULLSCREEN) {
            monitor = NULL;
        }

        return assertNotEquals(glfwCreateWindow(width, height, assertNonNull(title), monitor, NULL), NULL);
    }

}

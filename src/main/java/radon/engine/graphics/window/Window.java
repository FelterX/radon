package radon.engine.graphics.window;

import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;
import radon.engine.images.Image;
import radon.engine.images.PixelFormat;
import radon.engine.util.geometry.Rect;
import radon.engine.util.geometry.Rectc;
import radon.engine.util.geometry.Size;
import radon.engine.util.geometry.Sizec;
import radon.engine.util.handles.LongHandle;
import radon.engine.util.types.Singleton;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static radon.engine.util.Asserts.*;

public final class Window implements LongHandle {

    @Singleton
    private static Window instance;
    final CallbackManager callbacks;
    private final long handle;
    private final int defaultWidth;
    private final int defaultHeight;

    private String title;
    private Vector2i position;
    private Size size;
    private Size framebufferSize;
    private Rect rect;
    private DisplayMode displayMode;

    {
        if (instance != null) {
            throw new ExceptionInInitializerError("Window has been already created");
        }
    }

    Window(long handle, String title, DisplayMode displayMode, Sizec defaultSize) {

        this.handle = handle;
        this.title = title;
        this.displayMode = displayMode;
        this.defaultWidth = Math.max(defaultSize.width(), 1);
        this.defaultHeight = Math.max(defaultSize.height(), 1);

        position = new Vector2i();
        this.size = new Size();
        framebufferSize = new Size();
        rect = new Rect();

        callbacks = new CallbackManager().setup(this);

        update();
    }

    public static Window get() {
        return instance;
    }

    @Override
    public long handle() {
        return handle;
    }

    public String title() {
        return title;
    }

    public Window title(String title) {
        glfwSetWindowTitle(handle, title);
        return this;
    }

    public int x() {
        return position().x();
    }

    public int y() {
        return position().y();
    }

    public Vector2ic position() {
        return position;
    }

    public Window position(int x, int y) {
        glfwSetWindowPos(handle, x, y);
        return this;
    }

    public Window center() {

        GLFWVidMode vmode = assertNonNull(glfwGetVideoMode(glfwGetPrimaryMonitor()));

        Sizec size = size();

        return position(centerX(vmode.width(), size.width()), centerY(vmode.height(), size.height()));
    }

    public int width() {
        return size().width();
    }

    public int height() {
        return size().height();
    }

    public float aspect() {
        return size().aspect();
    }

    public Sizec size() {
        return size;
    }

    public Window size(int width, int height) {
        glfwSetWindowSize(handle, width, height);
        return this;
    }

    public Sizec framebufferSize() {
        return framebufferSize;
    }

    public Rectc rect() {
        return rect;
    }

    public CursorType cursorType() {
        return CursorType.of(glfwGetInputMode(handle, GLFW_CURSOR));
    }

    public Window cursorType(CursorType cursorType) {
        glfwSetInputMode(handle, GLFW_CURSOR, cursorType.glfwInputMode());
        return this;
    }

    public Window icon(Image icon) {
        return icons(Collections.singletonList(icon));
    }

    public Window icons(List<Image> icons) {
        try (MemoryStack stack = stackPush()) {
            GLFWImage.Buffer images = GLFWImage.mallocStack(icons.size(), stack);

            for (int i = 0; i < icons.size(); i++) {
                Image icon = icons.get(i);
                assertEquals(icon.pixelFormat(), PixelFormat.RGBA);

                GLFWImage image = images.get(i);

                image.width(icon.width());
                image.height(icon.height());
                image.pixels(icon.pixels());
            }

            glfwSetWindowIcon(handle, images);
        }
        return this;
    }

    public Window focus() {
        glfwFocusWindow(handle);
        return this;
    }

    public long clipboardHandle() {
        return nglfwGetClipboardString(handle);
    }

    public String clipboard() {
        return glfwGetClipboardString(handle);
    }

    public Window clipboard(String clipboard) {
        glfwSetClipboardString(handle, clipboard);
        return this;
    }

    public Window clipboard(ByteBuffer clipboard) {
        glfwSetClipboardString(handle, clipboard);
        return this;
    }

    public DisplayMode displayMode() {
        return visible() ? displayMode : DisplayMode.MINIMIZED;
    }

    public Window displayMode(DisplayMode displayMode) {

        switch (displayMode) {
            case MINIMIZED:
                return hide();
            case FULLSCREEN:
                return fullscreen();
            case MAXIMIZED:
                return maximize();
            case WINDOWED:
                return windowed();
        }
        throw new IllegalArgumentException();
    }

    public Window fullscreen() {
        restore();
        displayMode = DisplayMode.FULLSCREEN;
        long monitor = assertNotEquals(glfwGetPrimaryMonitor(), NULL);
        GLFWVidMode vmode = assertNonNull(glfwGetVideoMode(monitor));
        glfwWindowHint(GLFW_RED_BITS, vmode.redBits());
        glfwWindowHint(GLFW_GREEN_BITS, vmode.greenBits());
        glfwWindowHint(GLFW_BLUE_BITS, vmode.blueBits());
        glfwWindowHint(GLFW_ALPHA_BITS, vmode.redBits());
        glfwWindowHint(GLFW_REFRESH_RATE, vmode.refreshRate());
        return changeDisplayMode(monitor, vmode.refreshRate(), 0, 0, vmode.width(), vmode.height());
    }

    public Window maximize() {

        restore();

        if (displayMode == DisplayMode.FULLSCREEN) {
            windowed();
        }

        displayMode = DisplayMode.MAXIMIZED;
        glfwMaximizeWindow(handle);

        return this;
    }

    public Window windowed() {

        restore();

        displayMode = DisplayMode.WINDOWED;

        GLFWVidMode vmode = assertNonNull(glfwGetVideoMode(glfwGetPrimaryMonitor()));

        return changeDisplayMode(NULL, vmode.refreshRate(),
                centerX(vmode.width(), defaultWidth),
                centerY(vmode.height(), defaultHeight),
                defaultWidth,
                defaultHeight);
    }

    public boolean visible() {
        return glfwGetWindowAttrib(handle, GLFW_VISIBLE) == GLFW_TRUE && width() >= 1 && height() >= 1;
    }

    public Window show() {
        glfwShowWindow(handle);
        return this;
    }

    public Window hide() {
        restore();
        glfwHideWindow(handle);
        return this;
    }

    public boolean open() {
        return !shouldClose();
    }

    public Window restore() {
        glfwRestoreWindow(handle);
        return this;
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(handle);
    }

    public Window close() {
        glfwSetWindowShouldClose(handle, true);
        return this;
    }

    public void destroy() {
        callbacks.release();
        glfwDestroyWindow(handle);
    }


    private int centerY(int monitorHeight, int windowHeight) {
        return centerPos(monitorHeight, windowHeight);
    }

    private int centerX(int monitorWidth, int windowWidth) {
        return centerPos(monitorWidth, windowWidth);
    }

    private int centerPos(int monitorSize, int windowSize) {
        return (monitorSize - windowSize) / 2;
    }

    private Window changeDisplayMode(long monitor, int refreshRate, int x, int y, int width, int height) {

        glfwSetWindowMonitor(
                handle,
                monitor,
                x,
                y,
                width,
                height,
                refreshRate);

        return this;
    }

    void update() {
        updatePosition();
        updateSize();
        updateRect();
        updateFramebufferSize();
    }

    private void updatePosition() {
        try (MemoryStack stack = stackPush()) {
            IntBuffer x = stack.mallocInt(1);
            IntBuffer y = stack.mallocInt(1);
            glfwGetWindowPos(handle, x, y);
            position.set(x.get(0), y.get(0));
        }
    }

    private void updateSize() {
        try (MemoryStack stack = stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            glfwGetWindowSize(handle, width, height);
            size.set(width.get(0), height.get(0));
        }
    }

    private void updateRect() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer left = stack.ints(0);
            IntBuffer top = stack.ints(0);
            IntBuffer right = stack.ints(0);
            IntBuffer bottom = stack.ints(0);
            glfwGetWindowFrameSize(handle, left, top, right, bottom);
            rect.set(left.get(0), right.get(0), top.get(0), bottom.get(0));
        }
    }

    private void updateFramebufferSize() {
        try (MemoryStack stack = stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            glfwGetFramebufferSize(handle, width, height);
            framebufferSize.set(width.get(0), height.get(0));
        }
    }

}

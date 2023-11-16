package radon.engine.graphics.window;

import static org.lwjgl.glfw.GLFW.*;

public enum CursorType {

    NORMAL(GLFW_CURSOR_NORMAL),
    HIDDEN(GLFW_CURSOR_HIDDEN),
    DISABLED(GLFW_CURSOR_DISABLED);

    private final int glfwInputMode;

    CursorType(int glfwInputMode) {
        this.glfwInputMode = glfwInputMode;
    }

    public static CursorType of(int glfwInputMode) {
        switch (glfwInputMode) {
            case GLFW_CURSOR_NORMAL:
                return NORMAL;
            case GLFW_CURSOR_HIDDEN:
                return HIDDEN;
            case GLFW_CURSOR_DISABLED:
                return DISABLED;
        }
        return null;
    }

    public int glfwInputMode() {
        return glfwInputMode;
    }

}

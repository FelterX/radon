package radon.engine.input;

import radon.engine.util.GLFWWrapper;
import radon.engine.util.collections.EnumMapper;

import static org.lwjgl.glfw.GLFW.*;

public enum State implements GLFWWrapper {

    PRESS(GLFW_PRESS),
    RELEASE(GLFW_RELEASE),
    REPEAT(GLFW_REPEAT),
    TYPE(3),
    CLICK(4);

    private static final EnumMapper<State, Integer> MAPPER;
    static {
        MAPPER = EnumMapper.of(State.class, GLFWWrapper::glfwHandle);
    }

    public static State asState(int id) {
        return MAPPER.keyOf(id);
    }

    private final int glfwHandle;

    State(int glfwHandle) {
        this.glfwHandle = glfwHandle;
    }

    @Override
    public int glfwHandle() {
        return glfwHandle;
    }
}

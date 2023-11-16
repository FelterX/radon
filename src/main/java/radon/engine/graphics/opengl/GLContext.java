package radon.engine.graphics.opengl;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import radon.engine.graphics.GraphicsContext;
import radon.engine.graphics.GraphicsFactory;
import radon.engine.graphics.opengl.skyboxpbr.GLSkyboxPBRTextureFactory;
import radon.engine.graphics.window.Window;
import radon.engine.scenes.environment.skybox.pbr.SkyboxPBRTextureFactory;
import radon.engine.util.handles.LongHandle;

import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;
import static radon.engine.core.RadonConfigConstants.*;
import static radon.engine.graphics.opengl.GLDebugMessenger.newGLDebugMessenger;

public class GLContext implements GraphicsContext, LongHandle {


    private long glContext;
    private GLDebugMessenger debugMessenger;
    private GLCapabilities capabilities;
    private GLGraphicsFactory graphicsFactory;
    private GLSkyboxPBRTextureFactory skyboxPBRTextureFactory;
    private GLMapper mapper;
    private boolean vsync;

    private GLContext() {

    }

    @Override
    public void init() {

        glContext = Window.get().handle();
        makeCurrent();

        capabilities = GL.createCapabilities();
        debugMessenger = newGLDebugMessenger();
        graphicsFactory = new GLGraphicsFactory(this);
        skyboxPBRTextureFactory = new GLSkyboxPBRTextureFactory(this);
        mapper = new GLMapper();

        glfwSwapInterval(VSYNC ? 1 : 0);

        if(MULTISAMPLE_ENABLE) {
            glEnable(GL_MULTISAMPLE);
        } else {
            glDisable(GL_MULTISAMPLE);
        }
    }

    @Override
    public boolean vsync() {
        return vsync;
    }

    @Override
    public void vsync(boolean vsync) {
        glfwSwapInterval(vsync ? 1 : 0);
        this.vsync = vsync;
    }

    @Override
    public GLMapper mapper() {
        return mapper;
    }

    @Override
    public GraphicsFactory graphicsFactory() {
        return graphicsFactory;
    }

    @Override
    public SkyboxPBRTextureFactory skyboxPBRTextureFactory() {
        return skyboxPBRTextureFactory;
    }

    @Override
    public long handle() {
        return glContext;
    }

    public GLCapabilities capabilities() {
        return capabilities;
    }

    private void makeCurrent() {
        glfwMakeContextCurrent(glContext);
    }

    @Override
    public void release() {

        graphicsFactory.release();

        if(OPENGL_ENABLE_DEBUG_MESSAGES) {
            debugMessenger.release();
        }

        GL.destroy();
    }
}

package radon.engine.graphics.opengl.shaders;

import radon.engine.graphics.ShaderStage;
import radon.engine.graphics.opengl.GLContext;
import radon.engine.graphics.opengl.GLObject;
import radon.engine.graphics.shaders.GLSLPreprocessor;
import radon.engine.logging.Log;

import java.nio.file.Path;

import static java.util.Objects.requireNonNull;
import static org.lwjgl.opengl.GL46C.*;

public final class GLShader extends GLObject {

    private final ShaderStage stage;
    private Path path;

    public GLShader(GLContext context, ShaderStage stage) {
        super(context, glCreateShader(stage.handle()));
        this.stage = stage;
    }

    public ShaderStage stage() {
        return stage;
    }

    public GLShader source(Path path) {
        this.path = requireNonNull(path);
        glShaderSource(handle(), new GLSLPreprocessor(path, stage).process());
        return this;
    }

    public GLShader compile() {
        glCompileShader(handle());
        checkCompilationStatus();
        return this;
    }

    private void checkCompilationStatus() {
        if (glGetShaderi(handle(), GL_COMPILE_STATUS) != GL_TRUE) {
            Log.fatal("Failed to compile OpenGL shader " + this + ":\n"
                    + glGetShaderInfoLog(handle()));
        }
    }

    @Override
    public void free() {
        glDeleteShader(handle());
        setHandle(NULL);
        path = null;
    }

    @Override
    public String toString() {
        return "GLShader{" +
                "handle()=" + handle() +
                ", stage=" + stage +
                ", path=" + path +
                '}';
    }
}

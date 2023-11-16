package radon.engine.graphics.opengl.rendering;

import radon.engine.graphics.opengl.shaders.GLShaderProgram;
import radon.engine.graphics.rendering.ShadingModel;
import radon.engine.resource.Resource;

import static java.util.Objects.requireNonNull;

public class GLShadingPipeline implements Resource {

    private final GLShaderProgram shader;
    private ShadingModel shadingModel;
    private boolean shadowsEnabled;

    public GLShadingPipeline(GLShaderProgram shader) {
        this(shader, null);
    }

    public GLShadingPipeline(GLShaderProgram shader, ShadingModel shadingModel) {
        this.shader = requireNonNull(shader);
        this.shadingModel = shadingModel;
    }

    public GLShaderProgram getShader() {
        return shader;
    }

    public ShadingModel getShadingModel() {
        return shadingModel;
    }

    public GLShadingPipeline setShadingModel(ShadingModel shadingModel) {
        this.shadingModel = shadingModel;
        return this;
    }

    public boolean areShadowsEnabled() {
        return shadowsEnabled;
    }

    public GLShadingPipeline setShadowsEnabled(boolean shadowsEnabled) {
        this.shadowsEnabled = shadowsEnabled;
        return this;
    }

    public boolean accept(ShadingModel otherShadingModel) {
        if(shadingModel == null) {
            return true;
        }
        return shadingModel == otherShadingModel;
    }

    @Override
    public void release() {
        shader.release();
    }
}

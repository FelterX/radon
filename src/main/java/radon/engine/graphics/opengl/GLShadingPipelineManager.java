package radon.engine.graphics.opengl;

import radon.engine.core.RadonFiles;
import radon.engine.graphics.opengl.rendering.GLShadingPipeline;
import radon.engine.graphics.opengl.shaders.GLShader;
import radon.engine.graphics.opengl.shaders.GLShaderProgram;
import radon.engine.graphics.rendering.ShadingModel;
import radon.engine.resource.Resource;
import radon.engine.scenes.SceneRenderInfo;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static radon.engine.graphics.ShaderStage.FRAGMENT_STAGE;
import static radon.engine.graphics.ShaderStage.VERTEX_STAGE;

public class GLShadingPipelineManager implements Resource {

    private static final Path PHONG_VERTEX_SHADER_PATH = RadonFiles.getPath("shaders/phong/phong.vert");
    private static final Path PHONG_FRAGMENT_SHADER_PATH = RadonFiles.getPath("shaders/phong/phong.frag");
    private static final Path PBR_METALLIC_VERTEX_SHADER_PATH = RadonFiles.getPath("shaders/pbr/metallic/pbr_metallic.vert");
    private static final Path PBR_METALLIC_FRAGMENT_SHADER_PATH = RadonFiles.getPath("shaders/pbr/metallic/pbr_metallic.frag");


    private final GLContext context;
    private final Map<ShadingModel, GLShadingPipeline> shadingPipelines;

    public GLShadingPipelineManager(GLContext context) {
        this.context = requireNonNull(context);
        shadingPipelines = new EnumMap<>(ShadingModel.class);
    }

    public GLShadingPipeline getShadingPipeline(SceneRenderInfo renderInfo) {
        GLShadingPipeline shadingPipeline = shadingPipelines.get(renderInfo.shadingModel());
        shadingPipeline.setShadowsEnabled(renderInfo.shadowsEnabled());
        return shadingPipeline;
    }

    @Override
    public void release() {
        shadingPipelines.values().forEach(GLShadingPipeline::release);
        shadingPipelines.clear();
    }

    public void init() {
        shadingPipelines.put(ShadingModel.PHONG, createPhongShadingPipeline());
        shadingPipelines.put(ShadingModel.PBR_METALLIC, createPBRMetallicShadingPipeline());
    }

    private GLShadingPipeline createPhongShadingPipeline() {
        return new GLShadingPipeline(new GLShaderProgram(context, "OpenGL Phong shader")
                .attach(new GLShader(context, VERTEX_STAGE).source(PHONG_VERTEX_SHADER_PATH).compile())
                .attach(new GLShader(context, FRAGMENT_STAGE).source(PHONG_FRAGMENT_SHADER_PATH).compile())
                .link(), ShadingModel.PHONG);
    }

    private GLShadingPipeline createPBRMetallicShadingPipeline() {
        return new GLShadingPipeline(new GLShaderProgram(context, "OpenGL PBR Metallic shader")
                .attach(new GLShader(context, VERTEX_STAGE).source(PBR_METALLIC_VERTEX_SHADER_PATH).compile())
                .attach(new GLShader(context, FRAGMENT_STAGE).source(PBR_METALLIC_FRAGMENT_SHADER_PATH).compile())
                .link(), ShadingModel.PBR_METALLIC);
    }

}

package radon.engine.graphics.opengl.rendering.renderers;

import org.joml.FrustumIntersection;
import radon.engine.core.Radon;
import radon.engine.graphics.opengl.GLContext;
import radon.engine.graphics.opengl.buffers.GLBuffer;
import radon.engine.graphics.opengl.rendering.GLShadingPipeline;
import radon.engine.graphics.opengl.rendering.culling.GLFrustumCuller;
import radon.engine.graphics.opengl.rendering.renderers.data.GLRenderData;
import radon.engine.graphics.opengl.rendering.shadows.GLShadowsInfo;
import radon.engine.graphics.opengl.shaders.GLShaderProgram;
import radon.engine.graphics.opengl.skyboxpbr.GLSkyboxStruct;
import radon.engine.graphics.rendering.ShadingModel;
import radon.engine.graphics.rendering.culling.FrustumCuller;
import radon.engine.graphics.rendering.culling.FrustumCullingPreCondition;
import radon.engine.materials.MaterialManager;
import radon.engine.materials.MaterialStorageHandler;
import radon.engine.scenes.Scene;
import radon.engine.scenes.components.meshes.MeshInstanceList;
import radon.engine.scenes.environment.skybox.Skybox;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static radon.engine.graphics.rendering.culling.FrustumCullingPreConditionState.CONTINUE;
import static radon.engine.graphics.rendering.culling.FrustumCullingPreConditionState.DISCARD;

public abstract class GLIndirectRenderer extends GLRenderer {

    public static final int FIRST_SKYBOX_TEXTURE_UNIT = 10;
    private static final String SHADOWS_ENABLED_UNIFORM_NAME = "u_ShadowsEnabled";
    private static final String DIR_SHADOW_MAPS_UNIFORM_NAME = "u_DirShadowMaps";
    private final GLShadowsInfo shadowsInfo;
    private final GLSkyboxStruct skyboxStruct;
    protected GLRenderData renderData;
    protected FrustumCuller frustumCuller;
    private Queue<Consumer<GLShaderProgram>> dynamicState;
    private int visibleObjects;

    public GLIndirectRenderer(GLContext context, GLShadowsInfo shadowsInfo) {
        super(context);
        this.shadowsInfo = shadowsInfo;
        skyboxStruct = new GLSkyboxStruct(context());
    }

    @Override
    public void init() {
        renderData = createRenderData();
        frustumCuller = new GLFrustumCuller(renderData);
        dynamicState = new ArrayDeque<>();
    }

    protected abstract GLRenderData createRenderData();

    @Override
    public void terminate() {
        renderData.release();
        frustumCuller.terminate();
        skyboxStruct.release();
    }

    public FrustumCuller frustumCuller() {
        return frustumCuller;
    }

    public void prepare(Scene scene) {
        renderData.update(scene, getInstances(scene));
    }

    public void addDynamicState(Consumer<GLShaderProgram> state) {
        dynamicState.add(state);
    }

    public void preComputeFrustumCulling(Scene scene) {
        visibleObjects = performFrustumCullingCPU(scene, FrustumCullingPreCondition.NO_PRECONDITION);
    }

    public void render(Scene scene, GLShadingPipeline shadingPipeline) {
        // Only draw those meshes with the current shading model
        final int drawCount = performFrustumCullingCPU(scene, (instance, meshView) ->
                shadingPipeline.accept(meshView.material().shadingModel()) ? CONTINUE : DISCARD);

        render(scene, drawCount, shadingPipeline);
    }

    public void renderPreComputedVisibleObjects(Scene scene, GLShadingPipeline shadingPipeline) {
        render(scene, visibleObjects, shadingPipeline);
    }

    public void render(Scene scene, int drawCount, GLShadingPipeline shadingPipeline) {

        if (drawCount <= 0) {
            dynamicState.clear();
            return;
        }

        final GLShaderProgram shader = shadingPipeline.getShader();

        shader.bind();

        setOpenGLState();

        bindShaderUniformsAndBuffers(scene, shadingPipeline);

        setDynamicState(shader);

        renderData.getVertexArray().bind();

        glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, NULL, drawCount, 0);

        shader.unbind();
    }

    public abstract MeshInstanceList<?> getInstances(Scene scene);

    protected void setOpenGLState() {
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
    }

    protected void bindShaderUniformsAndBuffers(Scene scene, GLShadingPipeline shadingPipeline) {

        final ShadingModel shadingModel = shadingPipeline.getShadingModel();
        final GLBuffer lightsUniformBuffer = scene.environment().buffer();
        MaterialStorageHandler<?> materialHandler = MaterialManager.get().getStorageHandler(shadingModel);
        final GLBuffer materialsBuffer = materialHandler.buffer();

        GLBuffer cameraUniformBuffer = scene.cameraInfo().cameraBuffer();
        if (Radon.isEditor() && !Radon.isRuntime()) {
            cameraUniformBuffer = scene.editorCameraInfo().cameraBuffer();
        }

        final Skybox skybox = scene.environment().skybox();

        cameraUniformBuffer.bind(GL_UNIFORM_BUFFER, 0);

        lightsUniformBuffer.bind(GL_UNIFORM_BUFFER, 1);

        renderData.getTransformsBuffer().bind(GL_SHADER_STORAGE_BUFFER, 2);

        materialsBuffer.bind(GL_SHADER_STORAGE_BUFFER, 3);

        shadowsInfo.buffer().bind(GL_UNIFORM_BUFFER, 5);

        if (shadingModel != ShadingModel.PHONG) {
            skyboxStruct.update(skybox).bind(6);
        }

        renderData.getCommandBuffer().bind(GL_DRAW_INDIRECT_BUFFER);
    }

    protected void setDynamicState(GLShaderProgram shader) {
        while (!dynamicState.isEmpty()) {
            dynamicState.poll().accept(shader);
        }
    }

    private int performFrustumCullingCPU(Scene scene, FrustumCullingPreCondition preCondition) {
        final FrustumIntersection frustum = scene.camera().frustum();
        return frustumCuller.performCullingCPU(frustum, getInstances(scene), preCondition);
    }
}

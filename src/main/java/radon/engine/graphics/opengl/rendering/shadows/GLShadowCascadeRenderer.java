package radon.engine.graphics.opengl.rendering.shadows;

import org.lwjgl.system.MemoryStack;
import radon.engine.graphics.opengl.GLContext;
import radon.engine.graphics.opengl.rendering.GLShadingPipeline;
import radon.engine.graphics.opengl.rendering.renderers.GLIndirectRenderer;
import radon.engine.graphics.opengl.rendering.renderers.GLMeshRenderer;
import radon.engine.graphics.opengl.shaders.GLShaderProgram;
import radon.engine.graphics.opengl.swapchain.GLFramebuffer;
import radon.engine.graphics.opengl.textures.GLTexture2D;
import radon.engine.graphics.rendering.culling.FrustumCuller;
import radon.engine.graphics.rendering.culling.FrustumCullingPreConditionState;
import radon.engine.graphics.rendering.shadows.ShadowCascade;
import radon.engine.lights.DirectionalLight;
import radon.engine.meshes.TerrainMesh;
import radon.engine.meshes.views.MeshView;
import radon.engine.scenes.Scene;
import radon.engine.scenes.components.meshes.MeshInstance;
import radon.engine.scenes.components.meshes.MeshInstanceList;

import java.nio.FloatBuffer;

import static java.lang.Math.max;
import static java.util.Objects.requireNonNull;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL14C.GL_DEPTH_COMPONENT32;
import static org.lwjgl.opengl.GL30C.GL_DEPTH_ATTACHMENT;
import static radon.engine.graphics.rendering.culling.FrustumCullingPreConditionState.CONTINUE;
import static radon.engine.graphics.rendering.culling.FrustumCullingPreConditionState.DISCARD;

public class GLShadowCascadeRenderer {

    private static final String LIGHT_MATRIX_UNIFORM_NAME = "u_LightProjectionViewMatrix";


    private final GLContext context;
    private final GLTexture2D depthTexture;
    private final GLFramebuffer framebuffer;
    private final ShadowCascade shadowCascade;
    private final GLShadingPipeline depthShadingPipeline;

    GLShadowCascadeRenderer(GLContext context, GLShadingPipeline depthShadingPipeline) {

        this.context = requireNonNull(context);
        this.depthShadingPipeline = depthShadingPipeline;

        depthTexture = new GLTexture2D(context);

        framebuffer = new GLFramebuffer(context);
        framebuffer.setAsDepthOnlyFramebuffer();

        shadowCascade = new ShadowCascade();
    }

    public ShadowCascade shadowCascade() {
        return shadowCascade;
    }

    public GLTexture2D depthTexture() {
        return depthTexture;
    }

    public void render(Scene scene, GLMeshRenderer meshRenderer, DirectionalLight light, float nearPlane, float farPlane) {

        shadowCascade.update(scene.camera(), nearPlane, farPlane, light);

        prepareFramebuffer(scene);

        renderMeshShadows(scene, meshRenderer.staticMeshRenderer());

        glFinish();
    }

    private void renderMeshShadows(Scene scene, GLIndirectRenderer renderer) {

        final MeshInstanceList<?> instances = renderer.getInstances(scene);

        final FrustumCuller frustumCuller = renderer.frustumCuller();

        final int drawCount = frustumCuller.performCullingCPU(shadowCascade.lightFrustum(), instances, this::discardTerrain);

        renderer.addDynamicState(this::setOpenGLStateAndUniforms);

        renderer.render(scene, drawCount, depthShadingPipeline);
    }

    private FrustumCullingPreConditionState discardTerrain(MeshInstance<?> instance, MeshView<?> meshView) {
        return meshView.mesh().getClass() == TerrainMesh.class ? DISCARD : CONTINUE;
    }

    private void setOpenGLStateAndUniforms(GLShaderProgram shader) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            shader.uniformMatrix4f(LIGHT_MATRIX_UNIFORM_NAME, false, shadowCascade.lightProjectionViewMatrix().get(buffer));
        }
    }

    private void prepareFramebuffer(Scene scene) {

        final int shadowMapSize = max(scene.environment().lighting().shadowMapSize(), 1);

        if(depthTexture.width() != shadowMapSize) {
            setupFramebuffer(shadowMapSize);
        }

        framebuffer.bind();

        glViewport(0, 0, shadowMapSize, shadowMapSize);
        glClearColor(0, 0, 0, 0);
        glClear(GL_DEPTH_BUFFER_BIT);
    }

    private void setupFramebuffer(int shadowMapSize) {
        depthTexture.reallocate(1, shadowMapSize, shadowMapSize, GL_DEPTH_COMPONENT32);
        framebuffer.attach(GL_DEPTH_ATTACHMENT, depthTexture, 0);
        framebuffer.ensureComplete();
    }

    public void terminate() {
        depthTexture.release();
        framebuffer.release();
    }
}

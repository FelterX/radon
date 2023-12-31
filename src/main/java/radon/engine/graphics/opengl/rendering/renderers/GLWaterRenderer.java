package radon.engine.graphics.opengl.rendering.renderers;

import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import radon.engine.core.RadonFiles;
import radon.engine.events.EventManager;
import radon.engine.events.window.WindowResizedEvent;
import radon.engine.graphics.opengl.GLContext;
import radon.engine.graphics.opengl.buffers.GLBuffer;
import radon.engine.graphics.opengl.rendering.GLShadingPipeline;
import radon.engine.graphics.opengl.shaders.GLShader;
import radon.engine.graphics.opengl.shaders.GLShaderProgram;
import radon.engine.graphics.opengl.swapchain.GLFramebuffer;
import radon.engine.graphics.opengl.textures.GLTexture2D;
import radon.engine.graphics.opengl.vertex.GLVertexArray;
import radon.engine.graphics.textures.Texture2D;
import radon.engine.graphics.window.Window;
import radon.engine.images.PixelFormat;
import radon.engine.materials.WaterMaterial;
import radon.engine.meshes.StaticMesh;
import radon.engine.meshes.views.WaterMeshView;
import radon.engine.scenes.Camera;
import radon.engine.scenes.Scene;
import radon.engine.scenes.components.meshes.MeshInstanceList;
import radon.engine.scenes.components.meshes.WaterMeshInstance;
import radon.engine.scenes.environment.SceneEnhancedWater;
import radon.engine.util.IColor;

import java.nio.FloatBuffer;
import java.util.Objects;
import java.util.function.Consumer;

import static java.lang.StrictMath.max;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31C.GL_UNIFORM_BUFFER;
import static org.lwjgl.system.MemoryStack.stackPush;
import static radon.engine.graphics.ShaderStage.FRAGMENT_STAGE;
import static radon.engine.graphics.ShaderStage.VERTEX_STAGE;
import static radon.engine.graphics.opengl.shaders.UniformUtils.uniformStructMember;
import static radon.engine.meshes.vertices.VertexLayouts.VERTEX_LAYOUT_3D;
import static radon.engine.util.handles.IntHandle.NULL;

public class GLWaterRenderer extends GLRenderer {

    public static final int QUAD_INDEX_COUNT = 6;
    private final GLMeshRenderer meshRenderer;
    private final GLSkyboxRenderer skyboxRenderer;
    private GLShadingPipeline waterShadingPipeline;
    private GLShadingPipeline sceneShadingPipeline;
    private GLVertexArray vertexArray;
    private GLBuffer vertexBuffer;
    private GLBuffer indexBuffer;
    private GLFramebuffer reflectionFramebuffer;
    private GLFramebuffer refractionFramebuffer;
    private GLTexture2D depthTexture;
    private StaticMesh quadMesh;
    private Vector4f clipPlane;
    private Consumer<GLShaderProgram> setClipPlaneUniform;

    public GLWaterRenderer(GLContext context, GLMeshRenderer meshRenderer, GLSkyboxRenderer skyboxRenderer) {
        super(context);
        this.meshRenderer = meshRenderer;
        this.skyboxRenderer = skyboxRenderer;
    }

    @Override
    public void init() {

        clipPlane = new Vector4f();

        setClipPlaneUniform = shader -> shader.uniformVector4f("u_ClipPlane", clipPlane);

        waterShadingPipeline = createWaterShadingPipeline();

        quadMesh = StaticMesh.quad();

        vertexArray = new GLVertexArray(context());

        vertexBuffer = new GLBuffer(context()).name("WATER VERTEX BUFFER");
        vertexBuffer.data(quadMesh.vertexData());

        indexBuffer = new GLBuffer(context()).name("WATER INDEX BUFFER");
        indexBuffer.data(quadMesh.indexData());

        vertexArray.addVertexBuffer(0, VERTEX_LAYOUT_3D.attributeList(0), vertexBuffer);
        vertexArray.setIndexBuffer(indexBuffer);

        createFramebuffers();

        EventManager.addEventCallback(WindowResizedEvent.class, this::recreateFramebuffers);
    }

    @Override
    public void terminate() {
        waterShadingPipeline.release();
        vertexArray.release();
        vertexBuffer.release();
        indexBuffer.release();
        reflectionFramebuffer.release();
        refractionFramebuffer.release();
        depthTexture.release();
        quadMesh = null;
    }

    public void render(Scene scene) {

        final MeshInstanceList<WaterMeshInstance> waterInstances = scene.meshInfo().getWaterMeshInstances();

        if (waterInstances == null) {
            return;
        }

        final GLShaderProgram shader = waterShadingPipeline.getShader();

        shader.bind();

        setOpenGLState(scene);

        vertexArray.bind();

        try (MemoryStack stack = stackPush()) {

            FloatBuffer modelMatrixBuffer = stack.mallocFloat(16);

            for (WaterMeshInstance instance : waterInstances) {

                renderWater(modelMatrixBuffer, instance);
            }
        }

        shader.unbind();

        glDisable(GL_BLEND);
    }

    private void renderWater(FloatBuffer modelMatrixBuffer, WaterMeshInstance instance) {

        final WaterMeshView view = instance.meshView();
        final WaterMaterial material = view.material();

        modelMatrixBuffer = instance.modelMatrix().get(modelMatrixBuffer);

        waterShadingPipeline.getShader().uniformMatrix4f("u_ModelMatrix", false, modelMatrixBuffer);

        setMaterialUniforms(material);

        glDrawElements(GL_TRIANGLES, QUAD_INDEX_COUNT, GL_UNSIGNED_INT, NULL);
    }

    private void setMaterialUniforms(WaterMaterial material) {

        final GLShaderProgram shader = waterShadingPipeline.getShader();

        final String matName = "u_Material";

        IColor color = material.getColor();

        GLTexture2D reflectionMap = (GLTexture2D) material.getReflectionMap();
        GLTexture2D refractionMap = (GLTexture2D) material.getRefractionMap();
        GLTexture2D dudvMap = (GLTexture2D) material.getDudvMap();
        GLTexture2D normalMap = (GLTexture2D) material.getNormalMap();

        Vector2fc tiling = material.tiling();

        final float distortionStrength = material.getDistortionStrength();
        final float textureOffset = material.getTextureOffset();
        final float colorStrength = material.getColorStrength();

        final int flags = material.flags();

        shader.uniformColorRGBA(uniformStructMember(matName, "color"), color);

        shader.uniformSampler(uniformStructMember(matName, "reflectionMap"), reflectionMap, 1);
        shader.uniformSampler(uniformStructMember(matName, "refractionMap"), refractionMap, 2);
        shader.uniformSampler(uniformStructMember(matName, "dudvMap"), dudvMap, 3);
        shader.uniformSampler(uniformStructMember(matName, "normalMap"), normalMap, 4);

        shader.uniformVector2f(uniformStructMember(matName, "tiling"), tiling);

        shader.uniformFloat(uniformStructMember(matName, "distortionStrength"), distortionStrength);
        shader.uniformFloat(uniformStructMember(matName, "textureOffset"), textureOffset);
        shader.uniformFloat(uniformStructMember(matName, "colorStrength"), colorStrength);

        shader.uniformInt(uniformStructMember(matName, "flags"), flags);
    }

    private void setOpenGLState(Scene scene) {

        final Camera camera = scene.camera();
        final GLBuffer cameraBuffer = scene.cameraInfo().cameraBuffer();
        final GLBuffer lightsBuffer = scene.environment().buffer();
        final GLShaderProgram shader = waterShadingPipeline.getShader();

        glEnable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        cameraBuffer.bind(GL_UNIFORM_BUFFER, 0);

        lightsBuffer.bind(GL_UNIFORM_BUFFER, 1);

        shader.uniformSampler("u_DepthMap", depthTexture, 0);
        shader.uniformFloat("u_NearPlane", camera.nearPlane());
        shader.uniformFloat("u_FarPlane", camera.farPlane());
    }

    public void bakeWaterTextures(Scene scene, GLShadingPipeline sceneShadingPipeline) {

        this.sceneShadingPipeline = sceneShadingPipeline;

        glEnable(GL_CLIP_DISTANCE0);

        final Camera camera = scene.camera();

        final MeshInstanceList<WaterMeshInstance> waterInstances = scene.meshInfo().getWaterMeshInstances();

        if (waterInstances == null) {
            return;
        }

        final SceneEnhancedWater enhancedWater = scene.enhancedWater();

        for (WaterMeshInstance instance : waterInstances) {

            final boolean underWater = camera.position().y() - instance.transform().position().y() < 0.0f;

            if (underWater) {
                bakeWaterTexturesUnderWater(scene, enhancedWater, instance);
            } else {
                bakeWaterTexturesNormally(scene, camera, enhancedWater, instance);
            }
        }

        clipPlane.set(0, 0, 0, 0);

        glDisable(GL_CLIP_DISTANCE0);
    }

    private void bakeWaterTexturesUnderWater(Scene scene, SceneEnhancedWater enhancedWater, WaterMeshInstance instance) {

        WaterMeshView waterView = instance.meshView();

        bakeWaterTexture(scene, enhancedWater, instance.meshView(), refractionFramebuffer, waterView.material().getRefractionMap(), true);
    }

    private void bakeWaterTexturesNormally(Scene scene, Camera camera, SceneEnhancedWater enhancedWater, WaterMeshInstance instance) {

        WaterMeshView waterView = instance.meshView();

        final float displacement = 2 * (camera.position().y() - instance.transform().position().y());

        bakeReflectionTexture(scene, camera, enhancedWater, waterView, displacement);

        bakeRefractionTexture(scene, camera, enhancedWater, waterView, displacement);
    }

    private void bakeReflectionTexture(Scene scene, Camera camera, SceneEnhancedWater enhancedWater, WaterMeshView waterView, float displacement) {
        prepareCameraToRenderWithReflectionPerspective(camera, waterView, displacement);
        bakeWaterTexture(scene, enhancedWater, waterView, reflectionFramebuffer, waterView.material().getReflectionMap(), true);
    }

    private void bakeRefractionTexture(Scene scene, Camera camera, SceneEnhancedWater enhancedWater, WaterMeshView waterView, float displacement) {
        prepareCameraToRenderWithRefractionPerspective(camera, waterView, displacement);
        bakeWaterTexture(scene, enhancedWater, waterView, refractionFramebuffer, waterView.material().getRefractionMap(), false);
    }

    private void prepareCameraToRenderWithReflectionPerspective(Camera camera, WaterMeshView waterView, float displacement) {

        Vector3fc position = camera.position();

        camera.position(position.x(), position.y() - displacement, position.z());

        camera.pitch(-camera.pitch());

        camera.updateMatrices();

        clipPlane.set(waterView.clipPlane());
        clipPlane.w *= -1;
    }

    private void prepareCameraToRenderWithRefractionPerspective(Camera camera, WaterMeshView waterView, float displacement) {

        Vector3fc position = camera.position();

        camera.position(position.x(), position.y() + displacement, position.z());

        camera.pitch(-camera.pitch());

        camera.updateMatrices();

        clipPlane.set(waterView.clipPlane());
        clipPlane.y *= -1;
    }

    private void bakeWaterTexture(Scene scene, SceneEnhancedWater enhancedWater, WaterMeshView waterView,
                                  GLFramebuffer framebuffer, Texture2D texture, boolean renderSkybox) {

        prepareFramebuffer(framebuffer, (GLTexture2D) texture);

        if (enhancedWater.isEnhanced(waterView)) {
            renderMeshes(scene, meshRenderer.staticMeshRenderer());
        }

        if (renderSkybox) {
            renderSkybox(scene);
        }

        glFinish();
    }

    private void renderMeshes(Scene scene, GLIndirectRenderer renderer) {
        renderer.addDynamicState(setClipPlaneUniform);
        renderer.render(scene, sceneShadingPipeline);
    }

    private void renderSkybox(Scene scene) {
        skyboxRenderer.render(scene);
    }

    private void prepareFramebuffer(GLFramebuffer framebuffer, GLTexture2D colorTexture) {

        final int width = max(Window.get().width(), 1);
        final int height = max(Window.get().height(), 1);

        if (colorTexture.width() != width || colorTexture.height() != height) {
            colorTexture.reallocate(1, width, height, PixelFormat.RGBA);
        }

        if (!Objects.equals(framebuffer.get(GL_COLOR_ATTACHMENT0), colorTexture)) {
            framebuffer.attach(GL_COLOR_ATTACHMENT0, colorTexture, 0);
            framebuffer.ensureComplete();
        }

        framebuffer.bind();

        glViewport(0, 0, width, height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    private void recreateFramebuffers(WindowResizedEvent e) {

        if (depthTexture != null) {

            depthTexture.release();
            reflectionFramebuffer.release();
            refractionFramebuffer.release();

            createFramebuffers();
        }
    }

    private void createFramebuffers() {

        final int width = max(Window.get().width(), 1);
        final int height = max(Window.get().height(), 1);

        createDepthTexture(width, height);

        reflectionFramebuffer = createFramebuffer();
        refractionFramebuffer = createFramebuffer();
    }

    private void createDepthTexture(int width, int height) {
        depthTexture = new GLTexture2D(context());
        depthTexture.reallocate(1, width, height, GL_DEPTH_COMPONENT24);
    }

    private GLFramebuffer createFramebuffer() {

        GLFramebuffer framebuffer = new GLFramebuffer(context());

        framebuffer.attach(GL_DEPTH_ATTACHMENT, depthTexture, 0);

        return framebuffer;
    }

    private GLShadingPipeline createWaterShadingPipeline() {
        return new GLShadingPipeline(createShader());
    }

    private GLShaderProgram createShader() {
        return new GLShaderProgram(context(), "OpenGL Water shader")
                .attach(new GLShader(context(), VERTEX_STAGE).source(RadonFiles.getPath("shaders/water/water.vert")).compile())
                .attach(new GLShader(context(), FRAGMENT_STAGE).source(RadonFiles.getPath("shaders/water/water.frag")).compile())
                .link();
    }
}

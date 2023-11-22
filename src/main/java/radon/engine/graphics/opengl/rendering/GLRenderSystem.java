package radon.engine.graphics.opengl.rendering;

import radon.engine.core.Radon;
import radon.engine.events.EventManager;
import radon.engine.events.window.WindowResizedEvent;
import radon.engine.graphics.Graphics;
import radon.engine.graphics.opengl.GLContext;
import radon.engine.graphics.opengl.GLShadingPipelineManager;
import radon.engine.graphics.opengl.rendering.renderers.*;
import radon.engine.graphics.opengl.rendering.shadows.GLShadowRenderer;
import radon.engine.graphics.opengl.swapchain.GLFramebuffer;
import radon.engine.graphics.opengl.swapchain.GLRenderbuffer;
import radon.engine.graphics.opengl.textures.GLTexture2D;
import radon.engine.graphics.rendering.APIRenderSystem;
import radon.engine.graphics.window.Window;
import radon.engine.images.PixelFormat;
import radon.engine.scenes.Scene;
import radon.engine.scenes.SceneRenderInfo;
import radon.engine.scenes.environment.SceneEnvironment;
import radon.engine.util.Color;
import radon.engine.util.geometry.Sizec;

import static java.lang.StrictMath.max;
import static java.util.Objects.requireNonNull;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.opengl.GL11.glFinish;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL30.*;

public final class GLRenderSystem implements APIRenderSystem {

    private static final int DEFAULT_FRAMEBUFFER = 0;

    private final GLContext context;
    // Renderers
    private final GLSkyboxRenderer skyboxRenderer;
    private final GLShadowRenderer shadowRenderer;
    private final GLMeshRenderer meshRenderer;
    private final GLWaterRenderer waterRenderer;
    private final GLSpriteRenderer spriteRenderer;
    private final GLTileMapRenderer tileMapRenderer;

    // Shading Pipelines
    private final GLShadingPipelineManager shadingPipelineManager;
    private GLFramebuffer mainFramebuffer;
    private GLShadingPipeline currentShadingPipeline;

    private GLRenderSystem() {

        this.context = (GLContext) requireNonNull(Graphics.graphicsContext());

        createMainFramebuffer();

        skyboxRenderer = new GLSkyboxRenderer(context);
        shadowRenderer = new GLShadowRenderer(context);
        meshRenderer = new GLMeshRenderer(context, shadowRenderer);
        waterRenderer = new GLWaterRenderer(context, meshRenderer, skyboxRenderer);
        spriteRenderer = new GLSpriteRenderer(context);
        tileMapRenderer = new GLTileMapRenderer(context);

        shadingPipelineManager = new GLShadingPipelineManager(context);

        EventManager.addEventCallback(WindowResizedEvent.class, this::recreateFramebuffer);
    }

    @Override
    public void init() {
        meshRenderer.init();
        skyboxRenderer.init();
        waterRenderer.init();
        shadowRenderer.init();
        spriteRenderer.init();
        tileMapRenderer.init();

        shadingPipelineManager.init();
    }

    @Override
    public void terminate() {
        shadowRenderer.terminate();
        waterRenderer.terminate();
        skyboxRenderer.terminate();
        meshRenderer.terminate();
        spriteRenderer.terminate();
    }

    public GLFramebuffer mainFramebuffer() {
        return mainFramebuffer;
    }

    @Override
    public void begin() {
        Sizec framebufferSize = Window.get().framebufferSize();
        glViewport(0, 0, framebufferSize.width(), framebufferSize.height());
    }

    @Override
    public void prepare(Scene scene) {
        setVsync(scene.renderInfo().vsync());

        currentShadingPipeline = getCurrentShadingPipeline(scene.renderInfo());

        meshRenderer.prepare(scene);

        if (currentShadingPipeline.areShadowsEnabled()) {
            shadowRenderer.render(scene, meshRenderer);
        }

        waterRenderer.bakeWaterTextures(scene, currentShadingPipeline);

        meshRenderer.preComputeFrustumCulling(scene);
    }

    @Override
    public void render(Scene scene) {

        SceneEnvironment environment = scene.environment();

        mainFramebuffer.bind();

        clear(environment.clearColor());

        meshRenderer.renderPreComputedVisibleObjects(scene, currentShadingPipeline);

        waterRenderer.render(scene);

        tileMapRenderer.render(scene);
        spriteRenderer.render(scene);

        if (environment.skybox() != null) {
            skyboxRenderer.render(scene);
        }

        mainFramebuffer.unbind();

    }

    @Override
    public void end() {
        glFinish();
        if (!Radon.isEditor()) {
            copyFramebufferToScreen();
        }
        glfwSwapBuffers(context.handle());
    }

    private void clear(Color color) {
        glClearColor(color.red(), color.green(), color.blue(), color.alpha());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    private void copyFramebufferToScreen() {
        Sizec windowSize = Window.get().size();
        GLFramebuffer.blit(mainFramebuffer.handle(), DEFAULT_FRAMEBUFFER, windowSize.width(), windowSize.height(), GL_COLOR_BUFFER_BIT, GL_NEAREST);
    }

    private void createMainFramebuffer() {

        final int width = max(Window.get().width(), 1);
        final int height = max(Window.get().height(), 1);

        mainFramebuffer = new GLFramebuffer(context);

      /*  GLTexture2DMSAA colorBuffer = new GLTexture2DMSAA(context);
        colorBuffer.allocate(MSAA_SAMPLES.get(), width, height, PixelFormat.RGBA);*/

        GLTexture2D colorBuffer = new GLTexture2D(context);
        colorBuffer.allocate(1, width, height, PixelFormat.RGBA);

        GLRenderbuffer depthStencilBuffer = new GLRenderbuffer(context);
       // depthStencilBuffer.storageMultisample(width, height, GL_DEPTH24_STENCIL8, MSAA_SAMPLES.get());
        depthStencilBuffer.storage(width, height, GL_DEPTH24_STENCIL8);

        mainFramebuffer.attach(GL_COLOR_ATTACHMENT0, colorBuffer, 0);
        mainFramebuffer.attach(GL_DEPTH_STENCIL_ATTACHMENT, depthStencilBuffer);

        mainFramebuffer.freeAttachmentsOnRelease(true);

        mainFramebuffer.ensureComplete();
    }

    private void recreateFramebuffer(WindowResizedEvent e) {
        if (mainFramebuffer != null) {
            mainFramebuffer.release();
            createMainFramebuffer();
        }
    }

    private GLShadingPipeline getCurrentShadingPipeline(SceneRenderInfo renderInfo) {
        return shadingPipelineManager.getShadingPipeline(renderInfo);
    }

    private void setVsync(boolean vsync) {
        glfwSwapInterval(vsync ? 1 : 0);
    }
}

package radon.engine.graphics.opengl.rendering.renderers;

import radon.engine.graphics.opengl.GLContext;
import radon.engine.graphics.opengl.rendering.GLShadingPipeline;
import radon.engine.graphics.opengl.rendering.shadows.GLShadowsInfo;
import radon.engine.scenes.Scene;

public class GLMeshRenderer extends GLRenderer {

    private final GLStaticMeshRenderer staticMeshRenderer;

    public GLMeshRenderer(GLContext context, GLShadowsInfo shadowsInfo) {
        super(context);
        this.staticMeshRenderer = new GLStaticMeshRenderer(context, shadowsInfo);
    }

    @Override
    public void init() {
        staticMeshRenderer.init();
    }

    public void prepare(Scene scene) {
        staticMeshRenderer.prepare(scene);
    }

    public void preComputeFrustumCulling(Scene scene) {
        staticMeshRenderer.preComputeFrustumCulling(scene);
    }

    public void render(Scene scene, GLShadingPipeline shadingPipeline) {
        staticMeshRenderer.render(scene, shadingPipeline);
    }

    public void renderPreComputedVisibleObjects(Scene scene, GLShadingPipeline shadingPipeline) {
        staticMeshRenderer.renderPreComputedVisibleObjects(scene, shadingPipeline);
    }

    public GLStaticMeshRenderer staticMeshRenderer() {
        return staticMeshRenderer;
    }

    @Override
    public void terminate() {
        staticMeshRenderer.terminate();
    }
}

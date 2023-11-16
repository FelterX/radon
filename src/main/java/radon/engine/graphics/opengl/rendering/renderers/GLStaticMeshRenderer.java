package radon.engine.graphics.opengl.rendering.renderers;

import radon.engine.graphics.opengl.GLContext;
import radon.engine.graphics.opengl.rendering.renderers.data.GLRenderData;
import radon.engine.graphics.opengl.rendering.renderers.data.GLStaticRenderData;
import radon.engine.graphics.opengl.rendering.shadows.GLShadowsInfo;
import radon.engine.scenes.Scene;
import radon.engine.scenes.components.meshes.MeshInstanceList;
import radon.engine.scenes.components.meshes.StaticMeshInstance;

public final class GLStaticMeshRenderer extends GLIndirectRenderer {

    public GLStaticMeshRenderer(GLContext context, GLShadowsInfo shadowsInfo) {
        super(context, shadowsInfo);
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    protected GLRenderData createRenderData() {
        return new GLStaticRenderData(context());
    }

    @Override
    public void terminate() {
        super.terminate();
    }

    @Override
    public MeshInstanceList<StaticMeshInstance> getInstances(Scene scene) {
        return scene.meshInfo().getStaticMeshInstances();
    }
}

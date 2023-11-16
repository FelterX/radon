package radon.engine.scenes;

import radon.engine.graphics.rendering.ShadingModel;

import static radon.engine.core.RadonConfigConstants.*;


public class SceneRenderInfo {

    private ShadingModel shadingModel;
    private boolean shadowsEnabled;
    private boolean vsync;

    public SceneRenderInfo() {
        shadingModel = SCENE_SHADING_MODEL;
        shadowsEnabled = SHADOWS_ENABLED_ON_START;
        vsync = VSYNC;
    }

    public ShadingModel shadingModel() {
        return shadingModel;
    }

    public SceneRenderInfo shadingModel(ShadingModel shadingModel) {
        this.shadingModel = shadingModel;
        return this;
    }

    public boolean shadowsEnabled() {
        return shadowsEnabled;
    }

    public SceneRenderInfo shadowsEnabled(boolean shadowsEnabled) {
        this.shadowsEnabled = shadowsEnabled;
        return this;
    }

    public boolean vsync() {
        return vsync;
    }

    public SceneRenderInfo vsync(boolean vsync) {
        this.vsync = vsync;
        return this;
    }
}

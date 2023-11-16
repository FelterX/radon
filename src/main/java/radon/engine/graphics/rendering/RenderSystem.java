package radon.engine.graphics.rendering;


import radon.engine.core.RadonSystem;
import radon.engine.core.RadonSystemManager;
import radon.engine.graphics.opengl.rendering.GLRenderSystem;
import radon.engine.util.types.Singleton;

import static java.util.Objects.requireNonNull;
import static radon.engine.util.types.TypeUtils.newInstance;

public final class RenderSystem extends RadonSystem {

    @Singleton
    private static RenderSystem instance;

    public static RenderSystem getInstance(){
        return instance;
    }

    private static APIRenderSystem apiRenderSystem;

    private RenderSystem(RadonSystemManager systemManager) {
        super(systemManager);
    }

    public APIRenderSystem getAPIRenderSystem() {
        return apiRenderSystem;
    }

    @Override
    protected void init() {
        // Only supporting OPENGL for now
        Class<? extends APIRenderSystem> apiRenderSystemClass = GLRenderSystem.class;
        apiRenderSystem = requireNonNull(newInstance(apiRenderSystemClass));
        apiRenderSystem.init();
    }

    @Override
    protected void terminate() {
        apiRenderSystem.terminate();
        apiRenderSystem = null;
    }
}

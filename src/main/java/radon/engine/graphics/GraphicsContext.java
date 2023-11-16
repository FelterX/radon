package radon.engine.graphics;

import radon.engine.resource.Resource;
import radon.engine.scenes.environment.skybox.pbr.SkyboxPBRTextureFactory;

public interface GraphicsContext extends Resource {

    void init();

    boolean vsync();

    void vsync(boolean vsync);

    GraphicsMapper mapper();
    GraphicsFactory graphicsFactory();

    SkyboxPBRTextureFactory skyboxPBRTextureFactory();
}

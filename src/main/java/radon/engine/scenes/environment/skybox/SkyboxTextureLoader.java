package radon.engine.scenes.environment.skybox;

import radon.engine.graphics.textures.Cubemap;
import radon.engine.images.PixelFormat;

import java.nio.file.Path;

public interface SkyboxTextureLoader {

    PixelFormat pixelFormat();

    SkyboxTextureLoader pixelFormat(PixelFormat pixelFormat);

    String imageExtension();

    SkyboxTextureLoader imageExtension(String extension);

    Cubemap loadSkyboxTexture(Path path);

}

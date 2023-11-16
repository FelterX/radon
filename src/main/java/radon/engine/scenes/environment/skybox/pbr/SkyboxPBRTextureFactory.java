package radon.engine.scenes.environment.skybox.pbr;

import radon.engine.graphics.textures.Cubemap;
import radon.engine.graphics.textures.Texture2D;
import radon.engine.images.PixelFormat;

import java.nio.file.Path;

public interface SkyboxPBRTextureFactory {

    Cubemap createEnvironmentMap(Path hdrTexturePath, int size, PixelFormat pixelFormat);

    Cubemap createIrradianceMap(Cubemap environmentMap, int size);

    Cubemap createPrefilterMap(Cubemap environmentMap, int size, float maxLOD);

    Texture2D createBRDFTexture(int size);
}

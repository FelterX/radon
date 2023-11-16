package radon.engine.scenes.environment.skybox;

import radon.engine.graphics.textures.Sampler;
import radon.engine.graphics.textures.Texture;

public class SkyboxHelper {

    public static <T extends Texture> T setSkyboxTextureSamplerParameters(T texture) {

        if (texture == null) {
            return null;
        }

        texture.sampler()
                .wrapMode(Sampler.WrapMode.CLAMP_TO_EDGE)
                .magFilter(Sampler.MagFilter.LINEAR)
                .minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR);

        return texture;
    }

}

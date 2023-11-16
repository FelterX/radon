package radon.engine.scenes.environment.skybox;

import radon.engine.graphics.GraphicsFactory;
import radon.engine.graphics.textures.Cubemap;
import radon.engine.images.Image;
import radon.engine.images.ImageFactory;
import radon.engine.images.PixelFormat;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toUnmodifiableMap;

public class SimpleSkyboxTextureLoader extends AbstractSkyboxTextureLoader {

    private static final Map<Cubemap.Face, String> DEFAULT_CUBEMAP_FACE_NAMES = Arrays.stream(Cubemap.Face.values())
            .collect(toUnmodifiableMap(Function.identity(), face -> face.name().toLowerCase()));


    private Map<Cubemap.Face, String> cubemapFaceNames;

    public SimpleSkyboxTextureLoader() {
        cubemapFaceNames = new EnumMap<>(Cubemap.Face.class);
        cubemapFaceNames.putAll(DEFAULT_CUBEMAP_FACE_NAMES);
    }

    @Override
    public Cubemap loadSkyboxTexture(Path skyboxFolder) {

        Cubemap cubemap = GraphicsFactory.get().newCubemap();

        return setupCubemapFaces(cubemap, skyboxFolder, pixelFormat());
    }

    public Map<Cubemap.Face, String> cubemapFaceNames() {
        return cubemapFaceNames;
    }

    private Cubemap setupCubemapFaces(Cubemap cubemap, Path folder, PixelFormat pixelFormat) {

        boolean notAllocated = true;

        for (Cubemap.Face face : Cubemap.Face.values()) {

            if (!cubemapFaceNames.containsKey(face)) {
                throw new RuntimeException("No name provided for cubemap face " + face);
            }

            final String faceName = getImagePath(cubemapFaceNames.get(face));

            try (Image image = ImageFactory.newImage(folder.resolve(faceName), pixelFormat)) {

                final int size = image.width();

                if (notAllocated) {
                    cubemap.allocate(1, size, pixelFormat);
                    notAllocated = false;
                }

                cubemap.update(face, 0, 0, 0, size, pixelFormat, image.pixels());
            }
        }

        cubemap.generateMipmaps();

        return cubemap;
    }
}

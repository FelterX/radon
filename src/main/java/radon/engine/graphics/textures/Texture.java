package radon.engine.graphics.textures;

import radon.engine.images.PixelFormat;
import radon.engine.resource.Resource;
import radon.engine.util.types.ByteSize;

import static java.lang.Math.max;
import static radon.engine.util.Maths.log2;
import static radon.engine.util.handles.LongHandle.NULL;
import static radon.engine.util.types.DataType.UINT64_SIZEOF;

@ByteSize.Static(Texture.SIZEOF)
public interface Texture extends Resource {

    static int calculateMipLevels(int width, int height) {
        return log2(max(width, height) + 1);
    }

    static long makeResident(Texture texture) {
        return texture == null ? NULL : texture.makeResident();
    }

    int SIZEOF = UINT64_SIZEOF;

    int useCount();

    void incrementUseCount();

    void decrementUseCount();

    void resetUseCount();

    long residentHandle();

    long makeResident();

    void makeNonResident();

    void forceMakeNonResident();

    Sampler sampler();

    PixelFormat internalFormat();

    PixelFormat format();

    void generateMipmaps();

    default Texture setQuality(Quality quality) {

        switch(quality) {
            case LOW:
                setTextureLowQuality();
                break;
            case MEDIUM:
                setTextureMediumQuality();
                break;
            case HIGH:
                setTextureHighQuality();
                break;
            case VERY_HIGH:
                setTextureVeryHighQuality();
                break;
        }

        return this;
    }

    private void setTextureVeryHighQuality() {

        generateMipmaps();

        sampler().magFilter(Sampler.MagFilter.LINEAR)
                .minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR)
                .lodBias(-2.0f)
                .maxAnisotropy(16.0f);
    }

    private void setTextureHighQuality() {

        generateMipmaps();

        sampler().magFilter(Sampler.MagFilter.LINEAR)
                .minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR)
                .lodBias(-0.5f)
                .maxAnisotropy(4.0f);
    }

    private void setTextureMediumQuality() {

        generateMipmaps();

        sampler().magFilter(Sampler.MagFilter.LINEAR)
                .minFilter(Sampler.MinFilter.LINEAR_MIPMAP_NEAREST)
                .lodBias(0.0f)
                .maxAnisotropy(1.0f);
    }

    private void setTextureLowQuality() {
        sampler().magFilter(Sampler.MagFilter.NEAREST)
                .minFilter(Sampler.MinFilter.NEAREST_MIPMAP_NEAREST)
                .lodBias(1.0f)
                .maxAnisotropy(1.0f);
    }


    enum Quality {
        LOW,
        MEDIUM,
        HIGH,
        VERY_HIGH
    }
}

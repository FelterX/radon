package radon.engine.graphics.textures;

import radon.engine.resource.Resource;

public interface Sampler extends Resource {

    default Sampler wrapMode(WrapMode wrapMode) {
        wrapModeS(wrapMode);
        wrapModeT(wrapMode);
        wrapModeR(wrapMode);
        return this;
    }

    WrapMode wrapModeS();
    Sampler wrapModeS(WrapMode wrapMode);

    WrapMode wrapModeT();
    Sampler wrapModeT(WrapMode wrapMode);

    WrapMode wrapModeR();
    Sampler wrapModeR(WrapMode wrapMode);

    MinFilter minFilter();
    Sampler minFilter(MinFilter minFilter);

    MagFilter magFilter();
    Sampler magFilter(MagFilter magFilter);

    float maxSupportedAnisotropy();

    float maxAnisotropy();
    Sampler maxAnisotropy(float maxAnisotropy);

    boolean compareEnable();
    Sampler compareEnable(boolean enable);

    CompareOperation compareOperation();
    Sampler compareOperation(CompareOperation compareOperation);

    float minLod();
    Sampler minLod(float minLod);

    float maxLod();
    Sampler maxLod(float maxLod);

    float lodBias();
    Sampler lodBias(float lodBias);

    BorderColor borderColor();
    Sampler borderColor(BorderColor borderColor);

    enum WrapMode {
        REPEAT,
        MIRRORED_REPEAT,
        CLAMP_TO_BORDER,
        CLAMP_TO_EDGE,
        MIRRORED_CLAMP_TO_EDGE
    }

    enum MinFilter {
        NEAREST_MIPMAP_NEAREST,
        NEAREST_MIPMAP_LINEAR,
        LINEAR_MIPMAP_NEAREST,
        LINEAR_MIPMAP_LINEAR
    }

    enum MagFilter {
        NEAREST,
        LINEAR
    }

    enum BorderColor {
        WHITE_INT_OPAQUE,
        BLACK_INT_OPAQUE,

        WHITE_FLOAT_OPAQUE,
        BLACK_FLOAT_OPAQUE,

        BLACK_INT_TRANSPARENT,
        BLACK_FLOAT_TRANSPARENT
    }

    enum CompareOperation {
        NEVER,
        LESS,
        EQUAL,
        LESS_OR_EQUAL,
        GREATER,
        NOT_EQUAL,
        GREATER_OR_EQUAL,
        ALWAYS
    }

}

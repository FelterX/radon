package radon.engine.graphics.opengl.textures;

import org.lwjgl.system.MemoryStack;
import radon.engine.graphics.opengl.GLContext;
import radon.engine.graphics.opengl.GLObject;
import radon.engine.graphics.textures.Sampler;
import radon.engine.graphics.textures.Texture;
import radon.engine.images.PixelFormat;
import radon.engine.logging.Log;

import static java.util.Objects.requireNonNull;
import static org.lwjgl.opengl.ARBBindlessTexture.*;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public abstract class GLTexture extends GLObject implements Texture, Sampler {

    protected int target;
    protected boolean allocated;
    private long residentHandle;
    private BorderColor borderColor;
    protected PixelFormat imageFormat;
    protected int useCount;

    public GLTexture(GLContext context, int target) {
        super(context, glCreateTextures(target));
        this.target = target;
    }

    @Override
    public void generateMipmaps() {
        glGenerateTextureMipmap(handle());
    }

    public int width() {
        return glGetTextureLevelParameteri(handle(), 0, GL_TEXTURE_WIDTH);
    }

    public int height() {
        return glGetTextureLevelParameteri(handle(), 0, GL_TEXTURE_HEIGHT);
    }

    public int size() {
        return width() * height() * internalFormat().sizeof();
    }

    @Override
    public PixelFormat internalFormat() {
        return mapFromAPI(PixelFormat.class, glGetTextureLevelParameteri(handle(), 0, GL_TEXTURE_INTERNAL_FORMAT));
    }

    @Override
    public PixelFormat format() {
        return imageFormat;
    }

    @Override
    public int useCount() {
        return useCount;
    }

    @Override
    public void incrementUseCount() {
        ++useCount;
    }

    @Override
    public void decrementUseCount() {
        --useCount;
    }

    @Override
    public void resetUseCount() {
        useCount = 0;
    }

    @Override
    public long residentHandle() {
        return residentHandle;
    }

    public long makeResident() {
        if(residentHandle == NULL) {
            residentHandle = glGetTextureHandleARB(handle());
            glMakeTextureHandleResidentARB(residentHandle);
        }
        return residentHandle;
    }

    public void makeNonResident() {
        if(residentHandle != NULL && useCount == 1) {
            resetUseCount();
            glMakeTextureHandleNonResidentARB(residentHandle());
        } else {
            Log.warning("Tried to make texture " + this + " non resident, but it is used by multiple owners.");
        }
    }

    @Override
    public void forceMakeNonResident() {
        if(residentHandle != NULL) {
            glMakeTextureHandleNonResidentARB(residentHandle());
        }
    }

    @Override
    public Sampler sampler() {
        return this;
    }

    public void bind(int unit) {
        glBindTextureUnit(unit, handle());
    }

    public void unbind(int unit) {
        glBindTextureUnit(unit, 0);
    }

    @Override
    public WrapMode wrapModeS() {
        return mapFromAPI(WrapMode.class, glGetTextureParameteri(handle(), GL_TEXTURE_WRAP_S));
    }

    @Override
    public Sampler wrapModeS(WrapMode wrapMode) {
        glTextureParameteri(handle(), GL_TEXTURE_WRAP_S, mapToAPI(wrapMode));
        return this;
    }

    @Override
    public WrapMode wrapModeT() {
        return mapFromAPI(WrapMode.class, glGetTextureParameteri(handle(), GL_TEXTURE_WRAP_T));
    }

    @Override
    public Sampler wrapModeT(WrapMode wrapMode) {
        glTextureParameteri(handle(), GL_TEXTURE_WRAP_T, mapToAPI(wrapMode));
        return this;
    }

    @Override
    public WrapMode wrapModeR() {
        return mapFromAPI(WrapMode.class, glGetTextureParameteri(handle(), GL_TEXTURE_WRAP_R));
    }

    @Override
    public Sampler wrapModeR(WrapMode wrapMode) {
        glTextureParameteri(handle(), GL_TEXTURE_WRAP_R, mapToAPI(wrapMode));
        return this;
    }

    @Override
    public MinFilter minFilter() {
        return mapFromAPI(MinFilter.class, glGetTextureParameteri(handle(), GL_TEXTURE_MIN_FILTER));
    }

    @Override
    public Sampler minFilter(MinFilter minFilter) {
        glTextureParameteri(handle(), GL_TEXTURE_MIN_FILTER, mapToAPI(minFilter));
        return this;
    }

    @Override
    public MagFilter magFilter() {
        return mapFromAPI(MagFilter.class, glGetTextureParameteri(handle(), GL_TEXTURE_MAG_FILTER));
    }

    @Override
    public Sampler magFilter(MagFilter magFilter) {
        glTextureParameteri(handle(), GL_TEXTURE_MAG_FILTER, mapToAPI(magFilter));
        return this;
    }

    @Override
    public float maxSupportedAnisotropy() {
        if(context().capabilities().GL_EXT_texture_filter_anisotropic) {
            return glGetTextureParameterf(handle(), GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
        }
        return 1.0f;
    }

    @Override
    public float maxAnisotropy() {
        if(context().capabilities().GL_EXT_texture_filter_anisotropic) {
            return glGetTextureParameterf(handle(), GL_TEXTURE_MAX_ANISOTROPY_EXT);
        }
        return 1.0f;
    }

    @Override
    public Sampler maxAnisotropy(float maxAnisotropy) {
        if(context().capabilities().GL_EXT_texture_filter_anisotropic) {
            glTextureParameterf(handle(), GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy);
        } else {
            Log.warning("Anisotropic filtering not supported in this device");
        }
        return this;
    }

    @Override
    public boolean compareEnable() {
        return glGetTextureParameteri(handle(), GL_TEXTURE_COMPARE_MODE) == GL_COMPARE_REF_TO_TEXTURE;
    }

    @Override
    public Sampler compareEnable(boolean enable) {
        glTextureParameteri(handle(), GL_TEXTURE_COMPARE_MODE, enable ? GL_COMPARE_REF_TO_TEXTURE : GL_COMPARE_R_TO_TEXTURE);
        return this;
    }

    @Override
    public CompareOperation compareOperation() {
        return mapFromAPI(CompareOperation.class, glGetTextureParameteri(handle(), GL_TEXTURE_COMPARE_FUNC));
    }

    @Override
    public Sampler compareOperation(CompareOperation compareOperation) {
        glTextureParameteri(handle(), GL_TEXTURE_COMPARE_FUNC, mapToAPI(compareOperation));
        return this;
    }

    @Override
    public float minLod() {
        return glGetTextureParameterf(handle(), GL_TEXTURE_MIN_LOD);
    }

    @Override
    public Sampler minLod(float minLod) {
        glTextureParameterf(handle(), GL_TEXTURE_MIN_LOD, minLod);
        return this;
    }

    @Override
    public float maxLod() {
        return glGetTextureParameterf(handle(), GL_TEXTURE_MAX_LOD);
    }

    @Override
    public Sampler maxLod(float maxLod) {
        glTextureParameterf(handle(), GL_TEXTURE_MAX_LOD, maxLod);
        return this;
    }

    @Override
    public float lodBias() {
        return glGetTextureParameterf(handle(), GL_TEXTURE_LOD_BIAS);
    }

    @Override
    public Sampler lodBias(float lodBias) {
        glTextureParameterf(handle(), GL_TEXTURE_LOD_BIAS, lodBias);
        return this;
    }

    @Override
    public BorderColor borderColor() {
        return requireNonNull(borderColor);
    }

    @Override
    public Sampler borderColor(BorderColor borderColor) {
        this.borderColor = borderColor;
        setSamplerBorderColor(borderColor);
        return this;
    }

    @Override
    protected void free() {

        forceMakeNonResident();

        glDeleteTextures(handle());

        allocated = false;

        setHandle(NULL);
    }

    private void setSamplerBorderColor(BorderColor borderColor) {
        try (MemoryStack stack = stackPush()) {

            switch (borderColor) {
                case WHITE_INT_OPAQUE:
                    glTextureParameterIiv(handle(), GL_TEXTURE_BORDER_COLOR, stack.ints(1, 1, 1, 1));
                    break;
                case BLACK_INT_OPAQUE:
                    glTextureParameterIiv(handle(), GL_TEXTURE_BORDER_COLOR, stack.ints(0, 0, 0, 1));
                    break;
                case WHITE_FLOAT_OPAQUE:
                    glTextureParameterfv(handle(), GL_TEXTURE_BORDER_COLOR, stack.floats(1, 1, 1, 1));
                    break;
                case BLACK_FLOAT_OPAQUE:
                    glTextureParameterfv(handle(), GL_TEXTURE_BORDER_COLOR, stack.floats(0, 0, 0, 1));
                    break;
                case BLACK_INT_TRANSPARENT:
                    glTextureParameterIiv(handle(), GL_TEXTURE_BORDER_COLOR, stack.ints(0, 0, 0, 0));
                    break;
                case BLACK_FLOAT_TRANSPARENT:
                    glTextureParameterfv(handle(), GL_TEXTURE_BORDER_COLOR, stack.floats(0, 0, 0, 0));
                    break;
            }
        }
    }
}

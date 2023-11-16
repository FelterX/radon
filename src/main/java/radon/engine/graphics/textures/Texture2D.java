package radon.engine.graphics.textures;

import radon.engine.graphics.GraphicsFactory;
import radon.engine.images.Image;
import radon.engine.images.PixelFormat;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static radon.engine.graphics.textures.Texture.calculateMipLevels;

public interface Texture2D extends Texture {

    static Texture2D whiteTexture() {
        return GraphicsFactory.get().whiteTexture();
    }

    int width();

    int height();

    default void allocate(int width, int height, PixelFormat internalFormat) {
        allocate(calculateMipLevels(width, height), width, height, internalFormat);
    }

    void allocate(int mipLevels, int width, int height, PixelFormat internalFormat);

    void reallocate(int mipLevels, int width, int height, PixelFormat internalPixelFormat);

    default void pixels(Image image) {
        pixels(calculateMipLevels(image.width(), image.height()), image);
    }

    default void pixels(int mipLevels, Image image) {
        pixels(mipLevels, image.width(), image.height(), image.pixelFormat(), image.pixels());
    }

    default void pixels(int width, int height, PixelFormat format, ByteBuffer pixels) {
        pixels(calculateMipLevels(width, height), width, height, format, pixels);
    }

    default void pixels(int width, int height, PixelFormat format, FloatBuffer pixels) {
        pixels(calculateMipLevels(width, height), width, height, format, pixels);
    }

    void pixels(int mipLevels, int width, int height, PixelFormat format, ByteBuffer pixels);

    void pixels(int mipLevels, int width, int height, PixelFormat format, FloatBuffer pixels);

    void update(int mipLevel, int xOffset, int yOffset, int width, int height, PixelFormat format, ByteBuffer pixels);

    void update(int mipLevel, int xOffset, int yOffset, int width, int height, PixelFormat format, FloatBuffer pixels);

    @Override
    default Texture2D setQuality(Quality quality) {
        Texture.super.setQuality(quality);
        return this;
    }
}

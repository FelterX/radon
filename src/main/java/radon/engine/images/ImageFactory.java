package radon.engine.images;

import org.lwjgl.system.MemoryStack;
import radon.engine.logging.Log;
import radon.engine.util.FileUtils;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;

import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;

public final class ImageFactory {

    public static Image newWhiteImage(PixelFormat format) {
        return newWhiteImage(1, 1, format);
    }

    public static Image newBlackImage(PixelFormat format) {
        return newBlackImage(1, 1, format);
    }
    public static Image newWhiteImage(int width, int height, PixelFormat format) {
        return newImage(width, height, format, 0xFFFFFFFF);
    }

    public static Image newBlackImage(int width, int height, PixelFormat format) {
        return newImage(width, height, format, 0x0);
    }

    public static Image newImage(int width, int height, PixelFormat format, int pixelValue) {
        Image image = newImage(width, height, format);
        memSet(image.pixels(), pixelValue);
        return image;
    }

    public static Image newImage(int width, int height, PixelFormat format) {
        return newImage(width, height, format, memAlloc(width * height * format.sizeof()));
    }

    public static Image newImage(int width, int height, PixelFormat format, ByteBuffer buffer) {
        return new BufferedImage(width, height, format, buffer);
    }

    public static Image newIcon(Path path) {
        return newImage(path, PixelFormat.RGBA);
    }

    public static Image newImage(Path path, PixelFormat pixelFormat) {
        return newImage(path, pixelFormat, false);
    }

    public static Image newImage(Path path, PixelFormat pixelFormat, boolean flipY) {

        try(MemoryStack stack = stackPush()) {

            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);
            int desiredChannels = pixelFormat == null ? STBI_default : pixelFormat.channels();

            ByteBuffer pixels = readPixelsFromFile(path, width, height, channels, desiredChannels, pixelFormat, flipY);

            if(pixels == null) {
                Log.error("Failed to load image " + path + ": " + stbi_failure_reason());
                return null;
            }

            if(pixelFormat == null) {
                pixelFormat = PixelFormat.of(pixels, channels.get(0));
            }

            return new STBImage(width.get(0), height.get(0), pixelFormat, pixels);

        } catch(Throwable e) {
            Log.error("Failed to load image " + path + ": " + stbi_failure_reason(), e);
        }
        return null;
    }

    private static ByteBuffer readPixelsFromFile(Path path,
                                                 IntBuffer width, IntBuffer height,
                                                 IntBuffer channels, int desiredChannels,
                                                 PixelFormat pixelFormat, boolean flipY) {

        stbi_set_flip_vertically_on_load(flipY);

        ByteBuffer fileContents = FileUtils.readAllBytes(path);

        try {

            if(pixelFormat != null && pixelFormat.dataType().decimal()) {

                FloatBuffer pixelsf = stbi_loadf_from_memory(fileContents, width, height, channels, desiredChannels);

                if(pixelsf != null) {
                    return memByteBuffer(pixelsf);
                }

                return null;

            } else {
                return stbi_load_from_memory(fileContents, width, height, channels, desiredChannels);
            }

        } finally {
            memFree(fileContents);
        }
    }

    private static final class STBImage extends Image {

        STBImage(int width, int height, PixelFormat pixelFormat, ByteBuffer pixels) {
            super(width, height, pixelFormat, pixels);
        }

        @Override
        protected void free() {
            nstbi_image_free(memAddress((Buffer)pixels()));
        }
    }

    private static final class BufferedImage extends Image {

        BufferedImage(int width, int height, PixelFormat pixelFormat, ByteBuffer pixels) {
            super(width, height, pixelFormat, pixels);
        }

        @Override
        protected void free() {
            memFree(pixels());
        }
    }

    private ImageFactory() {}

}

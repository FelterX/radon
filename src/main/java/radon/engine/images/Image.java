package radon.engine.images;

import radon.engine.resource.ManagedResource;

import java.nio.ByteBuffer;

public abstract class Image extends ManagedResource {

    private final int width;
    private final int height;
    private final PixelFormat pixelFormat;
    private ByteBuffer pixels;

    Image(int width, int height, PixelFormat pixelFormat, ByteBuffer pixels) {
        this.width = width;
        this.height = height;
        this.pixelFormat = pixelFormat;
        this.pixels = pixels;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int channels() {
        return pixelFormat.channels();
    }

    public PixelFormat pixelFormat() {
        return pixelFormat;
    }

    public int size() {
        return width * height * pixelFormat.sizeof();
    }

    public ByteBuffer pixels() {
        return pixels;
    }
}

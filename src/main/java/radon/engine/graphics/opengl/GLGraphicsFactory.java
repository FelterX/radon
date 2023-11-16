package radon.engine.graphics.opengl;

import radon.engine.graphics.GraphicsFactory;
import radon.engine.graphics.buffers.IndexBuffer;
import radon.engine.graphics.buffers.StorageBuffer;
import radon.engine.graphics.buffers.UniformBuffer;
import radon.engine.graphics.buffers.VertexBuffer;
import radon.engine.graphics.opengl.buffers.GLBuffer;
import radon.engine.graphics.opengl.textures.GLCubemap;
import radon.engine.graphics.opengl.textures.GLTexture2D;
import radon.engine.graphics.opengl.textures.GLTexture2DMSAA;
import radon.engine.graphics.textures.Cubemap;
import radon.engine.graphics.textures.Texture2D;
import radon.engine.graphics.textures.Texture2DMSAA;
import radon.engine.images.Image;
import radon.engine.images.ImageFactory;
import radon.engine.images.PixelFormat;

import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

public class GLGraphicsFactory implements GraphicsFactory {

    private final GLContext context;
    private Texture2D whiteTexture2D;
    private Texture2D blackTexture2D;

    public GLGraphicsFactory(GLContext context) {
        this.context = requireNonNull(context);
    }

    @Override
    public Texture2D newTexture2D() {
        return new GLTexture2D(context);
    }

    @Override
    public Texture2D whiteTexture() {
        if(whiteTexture2D == null) {
            whiteTexture2D = newWhiteTexture2D();
        }
        return whiteTexture2D;
    }

    @Override
    public Texture2D blackTexture2D() {
        if(blackTexture2D == null) {
            blackTexture2D = newBlackTexture2D();
        }
        return blackTexture2D;
    }

    @Override
    public Texture2D newTexture2D(Path imagePath, PixelFormat pixelFormat) {
        return newTexture2D(imagePath, pixelFormat, false);
    }

    @Override
    public Texture2D newTexture2D(Path imagePath, PixelFormat pixelFormat, boolean flipY) {

        GLTexture2D texture = new GLTexture2D(context);

        try(Image image = ImageFactory.newImage(imagePath, pixelFormat, flipY)) {
            texture.pixels(requireNonNull(image));
        }

        texture.generateMipmaps();

        return texture;
    }

    @Override
    public Texture2DMSAA newTexture2DMSAA() {
        return new GLTexture2DMSAA(context);
    }

    @Override
    public Cubemap newCubemap() {
        return new GLCubemap(context);
    }

    @Override
    public StorageBuffer newStorageBuffer() {
        return new GLBuffer(context);
    }

    @Override
    public VertexBuffer newVertexBuffer() {
        return new GLBuffer(context);
    }

    @Override
    public IndexBuffer newIndexBuffer() {
        return new GLBuffer(context);
    }

    @Override
    public UniformBuffer newUniformBuffer() {
        return new GLBuffer(context);
    }

    private Texture2D newWhiteTexture2D() {

        Texture2D texture = newTexture2D();

        try(Image image = ImageFactory.newWhiteImage(PixelFormat.RGBA)) {
            texture.pixels(image);
        }

        return texture;
    }

    private Texture2D newBlackTexture2D() {

        Texture2D texture = newTexture2D();

        try(Image image = ImageFactory.newBlackImage(PixelFormat.RGBA)) {
            texture.pixels(image);
        }

        return texture;
    }

    @Override
    public void release() {
        if(whiteTexture2D != null) {
            whiteTexture2D.release();
        }
    }
}

package radon.engine.graphics;

import radon.engine.graphics.buffers.IndexBuffer;
import radon.engine.graphics.buffers.StorageBuffer;
import radon.engine.graphics.buffers.UniformBuffer;
import radon.engine.graphics.buffers.VertexBuffer;
import radon.engine.graphics.textures.Cubemap;
import radon.engine.graphics.textures.Texture2D;
import radon.engine.graphics.textures.Texture2DMSAA;
import radon.engine.images.PixelFormat;
import radon.engine.resource.Resource;

import java.nio.file.Path;

public interface GraphicsFactory extends Resource {

    static GraphicsFactory get() {
        return Graphics.graphicsContext().graphicsFactory();
    }

    Texture2D newTexture2D();

    Texture2D whiteTexture();

    Texture2D blackTexture2D();

    Texture2D newTexture2D(Path imagePath, PixelFormat pixelFormat);

    Texture2D newTexture2D(Path imagePath, PixelFormat pixelFormat, boolean flipY);

    Texture2DMSAA newTexture2DMSAA();

    Cubemap newCubemap();

    StorageBuffer newStorageBuffer();

    VertexBuffer newVertexBuffer();

    IndexBuffer newIndexBuffer();

    UniformBuffer newUniformBuffer();
}

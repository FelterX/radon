package radon.engine.graphics.buffers;

import radon.engine.graphics.GraphicsFactory;

public interface StorageBuffer extends MappedGraphicsBuffer {

    static StorageBuffer create() {
        return GraphicsFactory.get().newStorageBuffer();
    }

}

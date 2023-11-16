package radon.engine.graphics.buffers;

import org.lwjgl.BufferUtils;
import radon.engine.resource.Resource;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public interface GraphicsBuffer extends Resource {

    long size();

    default boolean allocated() {
        return size() > 0;
    }

    void allocate(long size);

    void reallocate(long newSize);

    void resize(long newSize); // Copy old data

    void data(ByteBuffer data);
    void data(IntBuffer data);
    void data(FloatBuffer data);

    default void clear() {
        clear(0);
    }

    void clear(int value);

    void update(long offset, ByteBuffer data);
    void update(long offset, IntBuffer data);
    void update(long offset, FloatBuffer data);

    ByteBuffer get(long offset, ByteBuffer buffer);

    default ByteBuffer get(long offset) {
        return get(offset, BufferUtils.createByteBuffer((int) size()));
    }
}

package radon.engine.graphics.opengl.buffers;

import radon.engine.graphics.buffers.*;
import radon.engine.graphics.opengl.GLContext;
import radon.engine.graphics.opengl.GLObject;
import radon.engine.logging.Log;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static java.lang.Math.min;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.system.libc.LibCString.nmemcpy;
import static org.lwjgl.system.libc.LibCString.nmemset;
import static radon.engine.util.types.DataType.FLOAT32_SIZEOF;
import static radon.engine.util.types.DataType.INT32_SIZEOF;

public class GLBuffer extends GLObject implements MappedGraphicsBuffer, VertexBuffer, IndexBuffer, StorageBuffer, UniformBuffer {

    private long size;
    private long memoryPtr;

    public GLBuffer(GLContext context) {
        super(context, glCreateBuffers());
    }

    @Override
    public long size() {
        return size;
    }

    public long nsize() {
        return glGetNamedBufferParameteri64(handle(), GL_BUFFER_SIZE);
    }

    public void bind(int target) {
        glBindBuffer(target, handle());
    }

    public void bind(int target, int binding) {
        glBindBufferBase(target, binding, handle());
    }

    public void bind(int target, int binding, long offset, long size) {
        glBindBufferRange(target, binding, handle(), offset, size);
    }

    public void unbind(int target, int index) {
        glBindBufferBase(target, index, 0);
    }

    public void unbind(int target) {
        glBindBuffer(target, 0);
    }

    @Override
    public void allocate(long size) {
        if(allocated()) {
            Log.fatal("This buffer is already allocated. Use reallocate instead");
            return;
        }
        glNamedBufferStorage(handle(), size, storageFlags());
        this.size = size;
    }

    @Override
    public void reallocate(long newSize) {
        if(size != newSize) {
            final boolean wasMapped = mapped();
            recreate();
            allocate(newSize);
            if(wasMapped) {
                mapMemory();
            }
        }
    }

    @Override
    public synchronized void resize(long newSize) {

        if(size == newSize) {
            return;
        }

        final boolean wasMapped = mapped();

        unmapMemory();

        final int srcBuffer = handle();
        final long oldSize = size;

        final int destBuffer = newBuffer(newSize);

        glCopyNamedBufferSubData(srcBuffer, destBuffer, 0, 0, min(newSize, oldSize));

        glFinish();

        release();

        setHandle(destBuffer);
        size = newSize;

        if(wasMapped) {
            mapMemory();
        }
    }

    @Override
    public void data(ByteBuffer data) {
        glNamedBufferStorage(handle(), data, storageFlags());
        this.size = data.remaining();
    }

    @Override
    public void data(FloatBuffer data) {
        glNamedBufferStorage(handle(), data, storageFlags());
        this.size = data.remaining() * FLOAT32_SIZEOF;
    }

    @Override
    public void data(IntBuffer data) {
        glNamedBufferStorage(handle(), data, storageFlags());
        this.size = data.remaining() * INT32_SIZEOF;
    }

    @Override
    public void clear(int value) {
        if(mapped()) {
            set(0, 0);
        } else {
            mapMemory();
            set(0, 0);
            unmapMemory();
        }
    }

    @Override
    public void update(long offset, ByteBuffer data) {
        glNamedBufferSubData(handle(), offset, data);
    }

    @Override
    public void update(long offset, FloatBuffer data) {
        glNamedBufferSubData(handle(), offset, data);
    }

    @Override
    public ByteBuffer get(long offset, ByteBuffer buffer) {
        glGetNamedBufferSubData(handle(), offset, buffer);
        return buffer;
    }

    @Override
    public void update(long offset, IntBuffer data) {
        glNamedBufferSubData(handle(), offset, data);
    }

    @Override
    public long nmappedMemoryPtr() {
        return memoryPtr;
    }

    @Override
    public void mapMemory(long offset, long size) {
        if(!allocated()) {
            Log.fatal("Buffer " + this + " has not been allocated");
            return;
        }
        if(mapped()) {
            Log.warning("Buffer " + this + " is already mapped");
            return;
        }

        memoryPtr = nglMapNamedBufferRange(handle(), offset, size, mapFlags());
    }

    @Override
    public void copy(long offset, long srcAddress, long size) {

        if(invalidMemoryRange(offset, size)) {
            return;
        }

        nmemcpy(memoryPtr + offset, srcAddress, size);
    }

    @Override
    public void set(long offset, int value, long size) {

        if(invalidMemoryRange(offset, size)) {
            return;
        }

        nmemset(memoryPtr + offset, value, size);
    }

    @Override
    public void unmapMemory() {
        if(mapped()) {
           glUnmapNamedBuffer(handle());
           memoryPtr = NULL;
        }
    }

    @Override
    public void free() {
        unmapMemory();
        glDeleteBuffers(handle());
        setHandle(NULL);
        size = 0;
    }

    protected int mapFlags() {
        return GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT;
    }

    protected int storageFlags() {
        return GL_DYNAMIC_STORAGE_BIT | mapFlags();
    }

    protected void recreate() {
        free();
        setHandle(glCreateBuffers());
    }

    private int newBuffer(long size) {

        final int buffer = glCreateBuffers();

        glNamedBufferStorage(buffer, size, storageFlags());

        return buffer;
    }

    private boolean invalidMemoryRange(long offset, long size) {

        if(!allocated()) {
            Log.fatal("Buffer " + this + " is not allocated");
            return true;
        }

        if(!mapped()) {
            Log.fatal("Buffer " + this + " is not mapped");
            return true;
        }

        final long dstAddress = memoryPtr + offset;

        if(dstAddress + size > mappedMemoryEndPtr()) {
            Log.fatal("Memory region is out of range: " + dstAddress + size + " > " + mappedMemoryEndPtr());
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return "GLBuffer{" +
                "name=" + name() +
                ", handle=" + handle() +
                ", size=" + size +
                ", memoryPtr=" + memoryPtr +
                '}';
    }
}

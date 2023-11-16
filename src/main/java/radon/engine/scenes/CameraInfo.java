package radon.engine.scenes;

import org.lwjgl.system.MemoryStack;
import radon.engine.graphics.GraphicsFactory;
import radon.engine.graphics.buffers.UniformBuffer;
import radon.engine.resource.Resource;

import java.nio.ByteBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static radon.engine.util.types.DataType.MATRIX4_SIZEOF;
import static radon.engine.util.types.DataType.VECTOR4_SIZEOF;

public final class CameraInfo implements Resource {

    public static final int CAMERA_BUFFER_SIZE = MATRIX4_SIZEOF + VECTOR4_SIZEOF;
    public static final int CAMERA_BUFFER_PROJECTION_VIEW_OFFSET = 0;
    public static final int CAMERA_BUFFER_CAMERA_POSITION_OFFSET = MATRIX4_SIZEOF;

    public static final int FRUSTUM_BUFFER_SIZE = MATRIX4_SIZEOF + 6 * VECTOR4_SIZEOF;
    public static final int FRUSTUM_BUFFER_PROJECTION_VIEW_OFFSET = 0;
    public static final int FRUSTUM_BUFFER_PLANES_OFFSET = MATRIX4_SIZEOF;


    private final UniformBuffer cameraBuffer;
    private final UniformBuffer frustumBuffer;

    public CameraInfo() {

        cameraBuffer = GraphicsFactory.get().newUniformBuffer();
        cameraBuffer.allocate(CAMERA_BUFFER_SIZE);
        cameraBuffer.mapMemory();

        frustumBuffer = GraphicsFactory.get().newUniformBuffer();
        frustumBuffer.allocate(FRUSTUM_BUFFER_SIZE);
        frustumBuffer.mapMemory();
    }

    @SuppressWarnings("unchecked")
    public <T extends UniformBuffer> T cameraBuffer() {
        return (T) cameraBuffer;
    }

    @SuppressWarnings("unchecked")
    public <T extends UniformBuffer> T frustumBuffer() {
        return (T) frustumBuffer;
    }

    @Override
    public void release() {
        cameraBuffer.release();
        frustumBuffer.release();
    }

    void update(Camera camera) {
        updateCameraBuffer(camera);
        updateFrustumBuffer(camera);
    }

    private void updateCameraBuffer(Camera camera) {

        try (MemoryStack stack = stackPush()) {

            ByteBuffer buffer = stack.calloc(CAMERA_BUFFER_SIZE);

            camera.projectionViewMatrix().get(CAMERA_BUFFER_PROJECTION_VIEW_OFFSET, buffer);
            camera.position().get(CAMERA_BUFFER_CAMERA_POSITION_OFFSET, buffer);

            cameraBuffer.copy(0, buffer);
        }
    }

    private void updateFrustumBuffer(Camera camera) {

        try (MemoryStack stack = stackPush()) {

            ByteBuffer buffer = stack.calloc(FRUSTUM_BUFFER_SIZE);

            camera.projectionViewMatrix().get(FRUSTUM_BUFFER_PROJECTION_VIEW_OFFSET, buffer);

            for (int i = 0; i < 6; i++) {
                camera.frustumPlanes()[i].get(FRUSTUM_BUFFER_PLANES_OFFSET + i * VECTOR4_SIZEOF, buffer);
            }

            frustumBuffer.copy(0, buffer);
        }
    }
}

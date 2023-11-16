package radon.engine.graphics.opengl.commands;

import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;
import radon.engine.graphics.opengl.rendering.renderers.data.GLRenderData;
import radon.engine.materials.ManagedMaterial;
import radon.engine.meshes.Mesh;
import radon.engine.meshes.views.MeshView;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import static org.lwjgl.system.MemoryStack.stackPush;
import static radon.engine.util.types.DataType.INT32_SIZEOF;
import static radon.engine.util.types.DataType.MATRIX4_SIZEOF;

public class GLCommandBuilder {

    private static final int INSTANCE_BUFFER_MIN_SIZE = INT32_SIZEOF * 2;

    private static final int TRANSFORMS_BUFFER_MIN_SIZE = MATRIX4_SIZEOF * 2;
    private static final int TRANSFORMS_BUFFER_MODEL_MATRIX_OFFSET = 0;
    private static final int TRANSFORMS_BUFFER_NORMAL_MATRIX_OFFSET = MATRIX4_SIZEOF;


    private final AtomicInteger baseInstance;
    private final GLRenderData renderData;

    public GLCommandBuilder(GLRenderData renderData) {
        this.baseInstance = new AtomicInteger();
        this.renderData = renderData;
    }

    public int count() {
        return baseInstance.getAndSet(0);
    }

    public void buildDrawCommand(GLDrawElementsCommand command, int matricesIndex, MeshView<?> meshView, Mesh mesh) {

        final int baseInstance = this.baseInstance.getAndIncrement();

        ManagedMaterial material = (ManagedMaterial) meshView.material();

        final int materialIndex = material.storageInfo().bufferIndex();

        setInstanceData(baseInstance, matricesIndex, materialIndex);

        command.count(mesh.indexCount())
                .primCount(1)
                .firstIndex(mesh.storageInfo().firstIndex())
                .baseVertex(mesh.storageInfo().baseVertex())
                .baseInstance(baseInstance);

        renderData.getCommandBuffer().copy(baseInstance * GLDrawElementsCommand.SIZEOF, command.buffer());
    }

    public void setInstanceTransform(int objectIndex, Matrix4fc modelMatrix, Matrix4fc normalMatrix) {

        try (MemoryStack stack = stackPush()) {

            ByteBuffer buffer = stack.malloc(MATRIX4_SIZEOF * 2);

            modelMatrix.get(TRANSFORMS_BUFFER_MODEL_MATRIX_OFFSET, buffer);
            normalMatrix.get(TRANSFORMS_BUFFER_NORMAL_MATRIX_OFFSET, buffer);

            renderData.getTransformsBuffer().copy(objectIndex * TRANSFORMS_BUFFER_MIN_SIZE, buffer);
        }
    }

    public void setInstanceData(int instanceID, int matrixIndex, int materialIndex) {

        try (MemoryStack stack = stackPush()) {

            IntBuffer buffer = stack.mallocInt(2);

            buffer.put(0, matrixIndex).put(1, materialIndex);

            renderData.getInstanceBuffer().copy(instanceID * INSTANCE_BUFFER_MIN_SIZE, buffer);
        }
    }

}

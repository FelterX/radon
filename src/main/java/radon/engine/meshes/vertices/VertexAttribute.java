package radon.engine.meshes.vertices;

import radon.engine.util.types.ByteSize;
import radon.engine.util.types.DataType;

import static java.util.Objects.requireNonNull;
import static radon.engine.util.types.DataType.FLOAT32;
import static radon.engine.util.types.DataType.INT32;

public enum VertexAttribute implements ByteSize {

    POSITION2D(FLOAT32, 2),
    POSITION3D(FLOAT32, 3),
    POSITION4D(FLOAT32, 4),
    NORMAL(FLOAT32, 3),
    TEXCOORDS2D(FLOAT32, 2),
    TEXCOORDS3D(FLOAT32, 3),
    TANGENTS(FLOAT32, 3),
    BITANGENTS(FLOAT32, 3),
    COLOR3D(FLOAT32, 3),
    COLOR4D(FLOAT32, 4),
    MATRIX4F(FLOAT32, 16),
    INDEX(INT32, 1),
    BONE_IDS(INT32, 4),
    BONE_WEIGHTS(FLOAT32, 4);

    private final DataType dataType;
    private final int size;

    VertexAttribute(DataType dataType, int size) {
        this.dataType = requireNonNull(dataType);
        this.size = size;
    }

    public DataType dataType() {
        return dataType;
    }

    public int size() {
        return size;
    }

    @Override
    public int sizeof() {
        return size * dataType.sizeof();
    }
}

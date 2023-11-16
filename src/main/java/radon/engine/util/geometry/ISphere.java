package radon.engine.util.geometry;

import org.joml.Vector3fc;
import radon.engine.util.types.ByteSize;

import java.nio.ByteBuffer;

import static radon.engine.util.types.DataType.FLOAT32_SIZEOF;


@ByteSize.Static(ISphere.SIZEOF)
public interface ISphere extends ByteSize {

    int SIZEOF = 4 * FLOAT32_SIZEOF;

    float centerX();

    float centerY();

    float centerZ();

    Vector3fc center();

    float radius();

    default int sizeof() {
        return SIZEOF;
    }

    ByteBuffer get(int offset, ByteBuffer buffer);
}

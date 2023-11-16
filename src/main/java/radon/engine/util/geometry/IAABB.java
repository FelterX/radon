package radon.engine.util.geometry;

import org.joml.Vector3fc;
import radon.engine.util.types.ByteSize;

import java.nio.ByteBuffer;

public interface IAABB extends ByteSize {

    Vector3fc min();

    Vector3fc max();

    float centerX();
    float centerY();
    float centerZ();

    ByteBuffer get(int offset, ByteBuffer buffer);
}

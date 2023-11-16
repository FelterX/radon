package radon.engine.scenes.environment;

import radon.engine.util.Color;
import radon.engine.util.IColor;
import radon.engine.util.types.ByteSize;

import java.nio.ByteBuffer;

import static java.util.Objects.requireNonNull;
import static radon.engine.util.Asserts.assertTrue;

@ByteSize.Static(Fog.SIZEOF)
public final class Fog implements ByteSize {

    public static final int SIZEOF = 20;

    public static final float DEFAULT_FOG_DENSITY = 0.16f;


    private final Color color;
    private float density;

    public Fog() {
        color = Color.colorBlackTransparent();
        density = DEFAULT_FOG_DENSITY;
    }

    public Color color() {
        return color;
    }

    public Fog color(IColor color) {
        this.color.set(requireNonNull(color));
        return this;
    }

    public float density() {
        return density;
    }

    public Fog density(float density) {
        this.density = density;
        return this;
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    public ByteBuffer get(int offset, ByteBuffer buffer) {
        assertTrue(buffer.position() + offset + SIZEOF <= buffer.limit());

        color.getRGBA(offset, buffer);
        buffer.putFloat(offset + Color.SIZEOF, density);

        return buffer;
    }
}

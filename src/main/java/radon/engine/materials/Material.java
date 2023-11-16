package radon.engine.materials;

import org.joml.Vector2fc;
import radon.engine.assets.Asset;
import radon.engine.graphics.rendering.ShadingModel;
import radon.engine.util.types.ByteSize;

public interface Material extends Asset, ByteSize {

    Vector2fc tiling();

    Material tiling(float x, float y);

    ShadingModel shadingModel();

    int flags();

    boolean destroyed();

}

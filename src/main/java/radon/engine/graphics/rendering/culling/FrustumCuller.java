package radon.engine.graphics.rendering.culling;

import org.joml.FrustumIntersection;
import radon.engine.scenes.components.meshes.MeshInstanceList;

public interface FrustumCuller {

    void init();

    void terminate();

    int performCullingCPU(FrustumIntersection frustum, MeshInstanceList<?> instances);

    int performCullingCPU(FrustumIntersection frustum, MeshInstanceList<?> instances, FrustumCullingPreCondition preCondition);

}

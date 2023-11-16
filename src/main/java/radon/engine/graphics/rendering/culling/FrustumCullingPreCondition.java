package radon.engine.graphics.rendering.culling;

import radon.engine.meshes.views.MeshView;
import radon.engine.scenes.components.meshes.MeshInstance;

public interface FrustumCullingPreCondition {

    FrustumCullingPreCondition NO_PRECONDITION = ((instance, meshView) -> FrustumCullingPreConditionState.CONTINUE);
    FrustumCullingPreCondition NEVER_PASS = ((instance, meshView) -> FrustumCullingPreConditionState.DISCARD);
    FrustumCullingPreCondition ALWAYS_PASS = ((instance, meshView) -> FrustumCullingPreConditionState.PASS);

    FrustumCullingPreConditionState compute(MeshInstance<?> instance, MeshView<?> meshView);
}

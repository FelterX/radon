package radon.engine.scenes.components.meshes;

import radon.engine.logging.Log;
import radon.engine.meshes.views.StaticMeshView;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import static radon.engine.util.Asserts.assertNonNull;
import static radon.engine.util.Asserts.assertTrue;

public class StaticMeshInstance extends MeshInstance<StaticMeshView> {

    @Override
    public Class<StaticMeshView> meshViewType() {
        return StaticMeshView.class;
    }

    public StaticMeshInstance meshViews(StaticMeshView... meshViews) {
        return meshViews(Arrays.asList(meshViews));
    }

    public StaticMeshInstance meshViews(Collection<StaticMeshView> meshViews) {
        if(this.meshViews != null) {
            Log.error("Cannot modify Mesh Instance component once you have set its Mesh Views");
        } else {
            assertNonNull(meshViews);
            assertTrue(meshViews.size() > 0);
            this.meshViews = meshViews.stream().filter(Objects::nonNull).distinct().collect(Collectors.toUnmodifiableList());
            doLater(() -> manager().enable(this));
        }
        return this;
    }
}

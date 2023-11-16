package radon.engine.scenes.components.meshes;

import radon.engine.meshes.views.WaterMeshView;

public class WaterMeshInstance extends MeshInstance<WaterMeshView> {

    @Override
    public Class<WaterMeshView> meshViewType() {
        return WaterMeshView.class;
    }
}

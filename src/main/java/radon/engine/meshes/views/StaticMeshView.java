package radon.engine.meshes.views;

import radon.engine.materials.Material;
import radon.engine.meshes.StaticMesh;

public class StaticMeshView extends MeshView<StaticMesh> {

    public StaticMeshView(StaticMesh mesh, Material material) {
        super(mesh, material);
    }
}

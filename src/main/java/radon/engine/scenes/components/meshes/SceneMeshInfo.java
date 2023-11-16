package radon.engine.scenes.components.meshes;

import radon.engine.meshes.views.MeshView;

import java.util.Map;

public interface SceneMeshInfo {

    Map<Class<? extends MeshView>, MeshInstanceList> allInstances();

    MeshInstanceList<StaticMeshInstance> getStaticMeshInstances();

    MeshInstanceList<WaterMeshInstance> getWaterMeshInstances();
}

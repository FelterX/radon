package radon.engine.scenes.components.meshes;

import radon.engine.meshes.views.MeshView;
import radon.engine.meshes.views.StaticMeshView;
import radon.engine.meshes.views.WaterMeshView;
import radon.engine.scenes.Scene;
import radon.engine.scenes.components.AbstractComponentManager;

import java.util.HashMap;
import java.util.Map;

public final class MeshInstanceManager extends AbstractComponentManager<MeshInstance> implements SceneMeshInfo {

    private final Map<Class<? extends MeshView>, MeshInstanceList> meshInstancesTable;

    protected MeshInstanceManager(Scene scene) {
        super(scene);
        meshInstancesTable = new HashMap<>();
    }

    @Override
    public Map<Class<? extends MeshView>, MeshInstanceList> allInstances() {
        return meshInstancesTable;
    }

    @SuppressWarnings("unchecked")
    @Override
    public MeshInstanceList<StaticMeshInstance> getStaticMeshInstances() {
        return meshInstancesTable.get(StaticMeshView.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MeshInstanceList<WaterMeshInstance> getWaterMeshInstances() {
        return meshInstancesTable.get(WaterMeshView.class);
    }

    @Override
    protected void add(MeshInstance meshInstance) {
        super.add(meshInstance);
    }

    @Override
    protected void enable(MeshInstance meshInstance) {
        super.enable(meshInstance);
        register(meshInstance);
    }

    @Override
    protected void disable(MeshInstance meshInstance) {
        super.disable(meshInstance);
        unregister(meshInstance);
    }

    @Override
    protected void remove(MeshInstance meshInstance) {
        super.remove(meshInstance);
        unregister(meshInstance);
    }

    @Override
    protected void removeAll() {
        super.removeAll();
        meshInstancesTable.clear();
    }

    @SuppressWarnings("unchecked")
    private void register(MeshInstance meshInstance) {

        Class<MeshView> meshViewType = meshInstance.meshViewType();

        MeshInstanceList meshInstancesOfThatType;

        if(meshInstancesTable.containsKey(meshViewType)) {
            meshInstancesOfThatType = meshInstancesTable.get(meshViewType);
        } else {
            meshInstancesOfThatType = new MeshInstanceList();
            meshInstancesTable.put(meshViewType, meshInstancesOfThatType);
        }

        meshInstancesOfThatType.add(meshInstance);
    }

    @SuppressWarnings("unchecked")
    private void unregister(MeshInstance meshInstance) {
        meshInstancesTable.get(meshInstance.meshViewType()).remove(meshInstance);
    }
}

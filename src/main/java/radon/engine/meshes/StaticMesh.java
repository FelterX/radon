package radon.engine.meshes;

import radon.engine.assets.Asset;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import static radon.engine.util.types.DataType.FLOAT32_SIZEOF;

public class StaticMesh extends Mesh implements Asset {

    public static final int VERTEX_DATA_SIZE = (3 + 3 + 2) * FLOAT32_SIZEOF;

    public static StaticMesh cube() {
        return MeshManager.get().get(PrimitiveMeshNames.CUBE_MESH_NAME);
    }

    public static StaticMesh quad() {
        return MeshManager.get().get(PrimitiveMeshNames.QUAD_MESH_NAME);
    }

    public static StaticMesh sphere() {
        return MeshManager.get().get(PrimitiveMeshNames.SPHERE_MESH_NAME);
    }

    public static StaticMesh get(String name, Consumer<MeshData> meshData) {

        MeshManager manager = MeshManager.get();

        if(manager.exists(name)) {
            return manager.get(name);
        }

        MeshData data = new MeshData();
        meshData.accept(data);

        return manager.createStaticMesh(name, data.vertices(), data.indices());
    }

    public StaticMesh(int handle, String name, ByteBuffer vertexData, ByteBuffer indexData) {
        super(handle, name, vertexData, indexData, VERTEX_DATA_SIZE);
    }

    @Override
    public Class<? extends Mesh> type() {
        return StaticMesh.class;
    }

}

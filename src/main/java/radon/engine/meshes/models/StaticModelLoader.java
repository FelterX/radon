package radon.engine.meshes.models;

import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import radon.engine.logging.Log;
import radon.engine.meshes.MeshManager;
import radon.engine.meshes.StaticMesh;
import radon.engine.util.FileUtils;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;
import static radon.engine.util.Asserts.assertNonNull;

public final class StaticModelLoader extends AssimpLoader {

    private static final int DEFAULT_FLAGS = aiProcess_OptimizeGraph
            | aiProcess_Triangulate
            | aiProcess_GenNormals
            | aiProcess_GenSmoothNormals
            | aiProcess_GenUVCoords
            | aiProcess_FlipUVs
            | aiProcess_JoinIdenticalVertices
            | aiProcess_FixInfacingNormals;


    private static final StaticVertexHandler DEFAULT_HANDLER = new StaticVertexHandler();
    private static final NameMapper DEFAULT_NAME_MAPPER = name -> name;


    private final Map<Path, StaticModel> cache;

    public StaticModelLoader() {
        cache = new HashMap<>();
    }

    public synchronized StaticModel load(Path path) {
        return load(path, DEFAULT_HANDLER, DEFAULT_NAME_MAPPER);
    }

    public synchronized StaticModel load(Path path, StaticVertexHandler handler) {
        return load(path, handler, DEFAULT_NAME_MAPPER);
    }

    public synchronized StaticModel load(Path path, NameMapper nameMapper) {
        return load(path, DEFAULT_HANDLER, nameMapper);
    }

    public synchronized StaticModel load(Path path, StaticVertexHandler handler, NameMapper nameMapper) {

        assertNonNull(handler);

        if (path == null) {
            Log.error("Model path cannot be null");
            return null;
        }

        if (Files.notExists(path)) {
            Log.error("File " + path + " does not exists");
            return null;
        }

        if (cache.containsKey(path)) {
            return cache.get(path);
        }

        float start = System.nanoTime();

        StaticModel model = loadAssimp(path, handler, nameMapper);

        float end = (float) ((System.nanoTime() - start) / 1e6);

        Log.trace("Model " + path.getName(path.getNameCount() - 1) + " loaded in " + end + " ms");

        cache.put(path, model);

        return model;
    }

    private StaticModel loadAssimp(Path path, StaticVertexHandler handler, NameMapper nameMapper) {

        ByteBuffer fileContents = FileUtils.readAllBytes(path);

        AIScene aiScene = aiImportFileFromMemory(fileContents, DEFAULT_FLAGS, FileUtils.getFileExtension(path));

        memFree(fileContents);

        try {

            if (aiScene == null || aiScene.mRootNode() == null) {
                Log.error("Could not load model: " + aiGetErrorString());
                return null;
            }

            StaticModel model = new StaticModel(path);

            PointerBuffer meshes = aiScene.mMeshes();

            for (int i = 0; i < meshes.capacity(); i++) {
                AIMesh aiMesh = requireNonNull(AIMesh.createSafe(meshes.get(i)));
                model.addMesh(loadMesh(aiScene, aiMesh, handler, nameMapper));
            }

            return model;

        } finally {
            aiReleaseImport(aiScene);
        }
    }

    private StaticMesh loadMesh(AIScene aiScene, AIMesh aiMesh, StaticVertexHandler handler, NameMapper nameMapper) {

        final String meshName = nameMapper.rename(aiMesh.mName().dataString());

        MeshManager meshManager = MeshManager.get();

        StaticMesh mesh = meshManager.get(meshName);

        if (mesh == null) {

            ByteBuffer vertices = memAlloc(StaticMesh.VERTEX_DATA_SIZE * aiMesh.mNumVertices());
            ByteBuffer indices = getIndices(aiMesh);

            processPositionAttribute(aiMesh, handler, vertices, StaticMesh.VERTEX_DATA_SIZE);
            processNormalAttribute(aiMesh, handler, vertices, StaticMesh.VERTEX_DATA_SIZE);
            processTexCoordsAttribute(aiMesh, handler, vertices, StaticMesh.VERTEX_DATA_SIZE);

            mesh = StaticMesh.get(meshName, staticMeshData -> staticMeshData.set(vertices, indices));
        }

        return mesh;
    }
}

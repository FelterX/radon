package radon.engine.assets;


import radon.engine.audio.AudioClipManager;
import radon.engine.core.RadonSystem;
import radon.engine.core.RadonSystemManager;
import radon.engine.materials.MaterialManager;
import radon.engine.meshes.MeshManager;
import radon.engine.util.types.Singleton;

import static radon.engine.util.types.TypeUtils.initSingleton;
import static radon.engine.util.types.TypeUtils.newInstance;

public class AssetsSystem extends RadonSystem {

    @Singleton
    private static AssetsSystem instance;

    private final MaterialManager materialManager;
    private final MeshManager meshManager;
    private final AudioClipManager audioClipManager;

    public AssetsSystem(RadonSystemManager systemManager) {

        super(systemManager);

        materialManager = newAssetManager(MaterialManager.class);
        meshManager = newAssetManager(MeshManager.class);
        audioClipManager = newAssetManager(AudioClipManager.class);
    }

    @Override
    protected void init() {
        materialManager.init();
        meshManager.init();
        audioClipManager.init();
    }

    @Override
    protected void terminate() {
        audioClipManager.terminate();
        meshManager.terminate();
        materialManager.terminate();
    }

    private <T extends AssetManager> T newAssetManager(Class<T> clazz) {
        T instance = newInstance(clazz);
        initSingleton(clazz, instance);
        return instance;
    }
}

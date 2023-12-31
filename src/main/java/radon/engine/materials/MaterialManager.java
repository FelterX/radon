package radon.engine.materials;

import radon.engine.assets.AssetManager;
import radon.engine.graphics.rendering.ShadingModel;
import radon.engine.logging.Log;
import radon.engine.util.types.Singleton;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class MaterialManager implements AssetManager<Material> {

    @Singleton
    private static MaterialManager instance;

    public static MaterialManager get() {
        return instance;
    }


    private final Map<String, Material> materials;
    private final Queue<Material> garbageQueue;
    private final Queue<Material> modificationsQueue;
    private final AtomicInteger handleProvider;
    private final Map<ShadingModel, MaterialStorageHandler> storageHandlers;

    public MaterialManager() {
        materials = new HashMap<>();
        garbageQueue = new ArrayDeque<>();
        modificationsQueue = new ArrayDeque<>();
        handleProvider = new AtomicInteger();
        storageHandlers = new EnumMap<>(ShadingModel.class);
    }

    @Override
    public void init() {
        storageHandlers.put(ShadingModel.PHONG, new PhongMaterialStorageHandler());
        storageHandlers.put(ShadingModel.PBR_METALLIC, new PBRMetallicMaterialStorageHandler());
    }

    @Override
    public int count() {
        return materials.size();
    }

    @SuppressWarnings("unchecked")
    public <T extends ManagedMaterial> MaterialStorageHandler<T> getStorageHandler(ShadingModel shadingModel) {
        return storageHandlers.get(shadingModel);
    }

    @Override
    public boolean exists(String assetName) {
        return materials.containsKey(assetName);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K extends Material> K get(String assetName) {
        return (K) materials.get(assetName);
    }

    public void addMaterial(Material material) {

        if(exists(material.name())) {
            Log.error("Material has been already added");
            return;
        }

        final int handle = handleProvider.getAndIncrement();

        addManagedMaterial(material);

        materials.put(material.name(), material);

        AbstractMaterial abstractMaterial = (AbstractMaterial) material;

        abstractMaterial.setHandle(handle);
        abstractMaterial.setMaterialManager(this);
    }

    public void update() {
        updateModifiedMaterials();
        destroyGarbage();
    }

    @Override
    public void destroy(Material material) {

        destroyManagedMaterial(material);

        materials.remove(material.name());
    }

    @Override
    public void destroyAll() {
        storageHandlers.values().forEach(MaterialStorageHandler::clear);
        materials.clear();
    }

    @Override
    public void terminate() {
        // destroyAll();
        storageHandlers.values().forEach(MaterialStorageHandler::terminate);
    }

    void markDestroyed(Material material) {
        garbageQueue.add(material);
    }

    void markModified(Material material) {
        modificationsQueue.add(material);
    }

    private void updateModifiedMaterials() {

        while(!modificationsQueue.isEmpty()) {

            AbstractMaterial material = (AbstractMaterial) modificationsQueue.poll();

            updateManagedMaterial(material);
        }
    }

    private void destroyGarbage() {

        while(!garbageQueue.isEmpty()) {

            Material material = garbageQueue.poll();

            destroy(material);
        }
    }

    @SuppressWarnings("unchecked")
    private void updateManagedMaterial(AbstractMaterial material) {

        if(material instanceof ManagedMaterial) {

            ManagedMaterial managedMaterial = (ManagedMaterial) material;

            MaterialStorageHandler storageHandler = storageHandlers.get(managedMaterial.shadingModel());

            storageHandler.update(managedMaterial);

            managedMaterial.markUpdated();
        }
    }

    @SuppressWarnings("unchecked")
    private void addManagedMaterial(Material material) {

        if(material instanceof ManagedMaterial) {

            ManagedMaterial managedMaterial = (ManagedMaterial) material;

            MaterialStorageHandler storageHandler = storageHandlers.get(managedMaterial.shadingModel());

            storageHandler.allocate(managedMaterial);
        }
    }

    @SuppressWarnings("unchecked")
    private void destroyManagedMaterial(Material material) {

        if(material instanceof ManagedMaterial) {

            ManagedMaterial managedMaterial = (ManagedMaterial) material;

            MaterialStorageHandler storageHandler = storageHandlers.get(material.shadingModel());

            storageHandler.free(managedMaterial);
        }
    }

}

package radon.engine.scenes;


import radon.engine.logging.Log;

import java.util.*;
import java.util.stream.Stream;

import static radon.engine.scenes.Entity.UNNAMED;
import static radon.engine.scenes.Entity.UNTAGGED;
import static radon.engine.util.types.TypeUtils.getOrElse;

public final class EntityManager implements Iterable<Entity> {

    private static final int INITIAL_CAPACITY = 32;

    private final Scene scene;
    private final List<Entity> entities;
    private final Queue<Integer> freeIndices;
    private final Map<String, Entity> nameTable;
    private final Map<String, List<Entity>> tagTable;

    EntityManager(Scene scene) {
        this.scene = scene;
        entities = new ArrayList<>(INITIAL_CAPACITY);
        freeIndices = new ArrayDeque<>(INITIAL_CAPACITY);
        nameTable = new HashMap<>(INITIAL_CAPACITY);
        tagTable = new HashMap<>(INITIAL_CAPACITY);
    }

    public Entity newEntity() {
        return newEntity(UNNAMED, UNTAGGED);
    }

    public Entity newEntity(String name, String tag) {

        if(nameTable.containsKey(name)) {
            Log.error("There is already an Entity named " + name + " in this scene. Names must be unique per scene");
            return null;
        }

        Entity entity;

        if(!freeIndices.isEmpty()) {

            entity = recycle(name, tag, freeIndices.poll());

        } else {

            entity = newEntity(name, tag, entities.size());
            entities.add(entity);
        }

        if(!Objects.equals(getOrElse(name, UNNAMED), UNNAMED)) {
            nameTable.put(name, entity);
        }

        if(!Objects.equals(getOrElse(tag, UNTAGGED), UNTAGGED)) {
            putInTagTable(tag, entity);
        }

        return entity;
    }

    private void putInTagTable(String tag, Entity entity) {
        tagTable.computeIfAbsent(tag, k -> new ArrayList<>()).add(entity);
    }

    public void remove(Entity entity) {

        if(!entity.name().equals(UNNAMED)) {
            nameTable.remove(entity.name());
        }

        if(!entity.tag().equals(UNTAGGED)) {
            tagTable.get(entity.tag()).remove(entity);
        }

        entities.set(entity.index(), null);
        freeIndices.add(entity.index());
    }

    public Entity find(String name) {
        return nameTable.get(name);
    }

    public Entity findWithTag(String tag) {
        if(!tagTable.containsKey(tag)) {
            return null;
        }
        return tagTable.get(tag).get(0);
    }

    public Stream<Entity> findAllWithTags(String tag) {
        if(!tagTable.containsKey(tag)) {
            return null;
        }
        return tagTable.get(tag).stream();
    }

    public boolean exists(String name) {
        return nameTable.containsKey(name);
    }

    public int entityCount() {
        return entities.size() - freeIndices.size();
    }

    public Stream<Entity> entities() {
        return entities.stream().filter(Objects::nonNull);
    }

    @Override
    public Iterator<Entity> iterator() {
        return entities().iterator();
    }

    void remove() {

        entities().forEach(Entity::onDestroy);

        entities.clear();
        freeIndices.clear();
        nameTable.clear();
        tagTable.clear();
    }

    private Entity recycle(String name, String tag, int index) {

        Entity entity = newEntity(name, tag, index);

        entities.set(index, entity);

        return entity;
    }

    private Entity newEntity(String name, String tag, int handle) {
        return new Entity(name, tag, scene, handle);
    }

}

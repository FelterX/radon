package radon.engine.scenes.components.sprites;

import radon.engine.scenes.ComponentManager;
import radon.engine.scenes.Scene;
import radon.engine.sprites.SceneSpriteInfo;

import java.util.ArrayList;
import java.util.List;

public class SpriteInstanceManager extends ComponentManager<SpriteInstance> implements SceneSpriteInfo {

    private final List<SpriteInstance> spriteInstanceList;
    private List<SpriteInstance> newInstances;
    private List<SpriteInstance> removeInstances;

    protected SpriteInstanceManager(Scene scene) {
        super(scene);
        this.spriteInstanceList = new ArrayList<>();
        this.newInstances = new ArrayList<>();
        this.removeInstances = new ArrayList<>();
    }

    @Override
    protected void add(SpriteInstance component) {
        spriteInstanceList.add(component);
        newInstances.add(component);
    }

    @Override
    protected void enable(SpriteInstance component) {
        newInstances.add(component);
    }

    @Override
    protected void disable(SpriteInstance component) {
        removeInstances.add(component);
    }

    @Override
    protected void remove(SpriteInstance component) {
        spriteInstanceList.remove(component);
    }

    @Override
    protected void removeAll() {
        spriteInstanceList.clear();
    }

    @Override
    protected int size() {
        return spriteInstanceList.size();
    }

    @Override
    public List<SpriteInstance> allInstances() {
        return spriteInstanceList;
    }

    @Override
    public List<SpriteInstance> newInstances() {
        return newInstances;
    }

    @Override
    public List<SpriteInstance> removeInstances() {
        return removeInstances;
    }

    @Override
    public void onAdded() {
        newInstances.clear();
    }  @Override
    public void onRemoved() {
        removeInstances.clear();
    }
}

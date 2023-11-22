package radon.engine.scenes.components.tilemap;

import radon.engine.scenes.ComponentManager;
import radon.engine.scenes.Scene;
import radon.engine.scenes.components.sprites.SpriteInstance;
import radon.engine.tiles.SceneTileMapInfo;

import java.util.ArrayList;
import java.util.List;

public class TileMapManager extends ComponentManager<TileMap> implements SceneTileMapInfo {

    private List<TileMap> maps = new ArrayList<>();
    private List<TileMap> newMaps = new ArrayList<>();

    protected TileMapManager(Scene scene) {
        super(scene);
    }

    public void update() {
        maps.forEach(TileMap::update);
    }

    @Override
    protected void add(TileMap component) {
        maps.add(component);
        newMaps.add(component);
    }

    @Override
    protected void enable(TileMap component) {
        maps.add(component);
    }

    @Override
    protected void disable(TileMap component) {
        maps.remove(component);
    }

    @Override
    protected void remove(TileMap component) {
        maps.remove(component);

    }

    @Override
    protected void removeAll() {
        maps.clear();
    }

    @Override
    protected int size() {
        return maps.size();
    }

    @Override
    public List<TileMap> allMaps() {
        return maps;
    }

    @Override
    public List<TileMap> newMaps() {
        return newMaps;
    }

    @Override
    public void onAdded() {
        newMaps.clear();
    }
}

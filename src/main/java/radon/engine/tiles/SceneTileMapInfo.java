package radon.engine.tiles;

import radon.engine.scenes.components.sprites.SpriteInstance;
import radon.engine.scenes.components.tilemap.TileMap;

import java.util.List;

public interface SceneTileMapInfo {
    List<TileMap> allMaps();
    List<TileMap> newMaps();

    void onAdded();

}

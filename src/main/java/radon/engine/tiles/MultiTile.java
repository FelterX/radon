package radon.engine.tiles;

import org.joml.Vector2i;
import radon.engine.sprites.Sprite;
import radon.engine.tiles.properties.TileProperty;

public class MultiTile extends Tile {
    public MultiTile(int id, String name, Sprite sprite, Vector2i size) {
        this(id, name, sprite, size, new Vector2i(0, 0));
    }

    public MultiTile(int id, String name, Sprite sprite, Vector2i size, Vector2i center) {
        super(id, name, sprite);

        this.addProperty(TileProperty.MULTI_TILE_SIZE, size);
        this.addProperty(TileProperty.MULTI_TILE_CENTER, center);
    }
}

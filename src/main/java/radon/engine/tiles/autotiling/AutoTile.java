package radon.engine.tiles.autotiling;

import radon.engine.sprites.Sprite;
import radon.engine.tiles.Tile;

import static radon.engine.tiles.properties.TileProperty.AUTO_TILING;

public class AutoTile extends Tile {

    public AutoTile(int id, String name, Sprite[] sprites) {
        super(id, name, sprites[0]);

        this.addProperty(AUTO_TILING, sprites);
    }
}

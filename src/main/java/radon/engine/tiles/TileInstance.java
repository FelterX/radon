package radon.engine.tiles;

import radon.engine.scenes.components.tilemap.TileMap;
import radon.engine.sprites.Sprite;
import radon.engine.tiles.autotiling.AutoTillingProcessor;

import static java.util.Objects.requireNonNull;

public class TileInstance {

    private final TileMap tileMap;
    private final Tile tile;
    private final int x;
    private final int y;

    private Sprite sprite;

    public TileInstance(TileMap tileMap, Tile tile, int x, int y) {
        this.tileMap = requireNonNull(tileMap);
        this.tile = requireNonNull(tile);
        this.x = x;
        this.y = y;
    }

    public void update() {
        boolean autoTiling = tile.autoTiling();
        if (autoTiling) AutoTillingProcessor.process(tileMap,this);
        else sprite = tile.sprite();
    }


    public TileMap tileMap() {
        return tileMap;
    }

    public Tile tile() {
        return tile;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public Sprite sprite() {
        return sprite;
    }

    public int i = 0;
    public void sprite(Sprite sprite, int i) {
        this.sprite = sprite;
        this.i = i ;
    }
}

package radon.engine.tiles;

import radon.engine.scenes.components.behaviours.Behaviour;
import radon.engine.scenes.components.sprites.SpriteInstance;
import radon.engine.scenes.components.tilemap.TileMap;
import radon.engine.sprites.Sprite;
import radon.engine.tiles.autotiling.AutoTillingProcessor;

import static java.util.Objects.requireNonNull;

public class TileInstance extends Behaviour {

    private TileMap tileMap;
    private Tile tile;
    private int x;
    private int y;

    private SpriteInstance spriteInstance;
    private Sprite sprite;

    public void setup(int x, int y, Tile tile, TileMap tileMap) {
        this.tileMap = requireNonNull(tileMap);
        this.tile = requireNonNull(tile);
        this.x = x;
        this.y = y;

        this.sprite = tile.sprite();
    }

    @Override
    protected void onStart() {
        super.onStart();

        this.spriteInstance = get(SpriteInstance.class);
        this.spriteInstance.sprite(sprite);
    }

    public void update() {
        boolean autoTiling = tile.autoTiling();
        if (autoTiling) AutoTillingProcessor.process(tileMap, this);
        else sprite(tile.sprite());
    }

    public void sprite(Sprite sprite) {
        if (started()) this.spriteInstance.sprite(sprite);
        else this.sprite = sprite;

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
}

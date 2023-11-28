package radon.engine.tiles.properties;

public class TileProperty<T> {

    public static final String AUTO_TILING = "tile.auto_tiling";
    public static final String MULTI_TILE_SIZE = "tile.multi_tile.size";
    public static final String MULTI_TILE_CENTER = "tile.multi_tile.center";

    private final String property;
    private T value;

    public TileProperty(String name, T value) {
        this.property = name;
        this.value = value;
    }

    public T value() {
        return value;
    }
}

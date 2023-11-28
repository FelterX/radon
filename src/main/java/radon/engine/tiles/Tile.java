package radon.engine.tiles;

import radon.engine.sprites.Sprite;
import radon.engine.tiles.properties.TileProperty;

import java.util.HashMap;


import static radon.engine.tiles.properties.TileProperty.AUTO_TILING;

public class Tile {

    private final int id;
    private final String name;
    private final Sprite sprite;

    protected HashMap<String, TileProperty<?>> properties = new HashMap<>();


    public Tile(int id, String name, Sprite sprite) {
        this.id = id;
        this.name = name;
        this.sprite = sprite;
    }

    public String name() {
        return name;
    }

    public Sprite sprite() {
        return sprite;
    }

    public <T> Tile addProperty(String property, T value) {
        TileProperty<T> tileProperty = new TileProperty<>(property, value);
        properties.put(property, tileProperty);
        return this;
    }

    public TileProperty<?> getProperty(String property) {
        return properties.get(property);
    }

    public <T> T getPropertyValue(String property) {
        if (properties.containsKey(property))
            return (T) properties.get(property).value();

        return null;
    }

    public boolean autoTiling() {
        return properties.containsKey(AUTO_TILING);
    }

    public int id() {
        return id;
    }
}

package radon.engine.tiles;

import radon.engine.sprites.Sprite;

public class Tile {
    private final String name;
    private final Sprite sprite;

    public Tile(String name, Sprite sprite) {
        this.name = name;
        this.sprite = sprite;
    }

    public String name() {
        return name;
    }

    public Sprite sprite() {
        return sprite;
    }
}

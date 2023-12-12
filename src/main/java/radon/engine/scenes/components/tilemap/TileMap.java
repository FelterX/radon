package radon.engine.scenes.components.tilemap;

import org.joml.Vector2i;
import radon.engine.scenes.Component;
import radon.engine.scenes.Entity;
import radon.engine.scenes.components.math.Transform;
import radon.engine.scenes.components.sprites.SpriteInstance;
import radon.engine.tiles.Tile;
import radon.engine.tiles.TileInstance;
import radon.engine.tiles.properties.TileProperty;

import java.util.*;

public class TileMap extends Component {

    private Map<Integer, Map<Integer, TileInstance>> tiles = new HashMap<>();
    private int layerOrder = 0;
    private float tileSize = 1 / 16f;
    private boolean modified = false;
    private List<Vector2i> updatedTiles = new ArrayList<>();
    private boolean useHardShadow = false;


    private TileInstance createTileInstance(int x, int y, Tile tile) {
        if (!contains(x, y)) {
            Entity tileEntity = scene().newEntity(entity().name() + ":tile_" + x + "," + y);

            Vector2i offset = tile.getPropertyValue(TileProperty.MULTI_TILE_CENTER);
            Vector2i size = tile.getPropertyValue(TileProperty.MULTI_TILE_SIZE);

            float posX = x;
            if (offset != null) x += offset.x;
            float posY = y;
            if (offset != null) y += offset.y;

            float sizeX = tileSize;
            if (size != null) sizeX *= size.x;
            float sizeY = tileSize;
            if (size != null) sizeY *= size.y;

            Transform transform = tileEntity.get(Transform.class);
            transform.translate(posX * sizeX, posY * sizeY, layerOrder);
            transform.scale(sizeX, sizeY, 1);
            entity().get(Transform.class).addChild(transform);

            tileEntity.add(TileInstance.class).setup(x, y, tile, this);
            tileEntity.add(SpriteInstance.class)
                    .sprite(tile.sprite())
                    .anchor(0, 0);

            return tileEntity.get(TileInstance.class);
        }

        return null;
    }

    protected void update() {
        if (modified()) {
            modified = false;
            updatedTiles.clear();
        }
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    @Override
    protected void onDestroy() {

    }

    public boolean contains(int x, int y) {
        Map<Integer, TileInstance> yMap = tiles.get(x);
        if (yMap == null) return false;
        return yMap.containsKey(y);
    }

    public Tile getTile(int x, int y) {
        if (contains(x, y)) return tiles.get(x).get(y).tile();
        return null;
    }

    public TileInstance getTileInstance(int x, int y) {
        if (contains(x, y)) return tiles.get(x).get(y);
        return null;
    }

    public void setTile(int x, int y, Tile tile) {
        Map<Integer, TileInstance> yMap = tiles.computeIfAbsent(x, k -> new HashMap<>());
        yMap.put(y, createTileInstance(x, y, tile));

        for (int i = -1; i < 2; i++)
            for (int j = -1; j < 2; j++)
                updateTile(x + i, y + j);
    }

    public void removeTile(int x, int y) {
        if (contains(x, y)) {
            Map<Integer, TileInstance> yMap = tiles.get(x);
            scene().destroy(yMap.get(y).entity());

            yMap.remove(y);
            if (yMap.isEmpty()) {
                tiles.remove(x);
            }


            for (int i = -1; i < 2; i++)
                for (int j = -1; j < 2; j++)
                    updateTile(x + i, y + j);

            modify(x, y);
        }
    }

    public void fill(int startX, int startY, int endX, int endY, Tile tile) {
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                if (contains(x, y)) {
                    Map<Integer, TileInstance> yMap = tiles.get(x);
                    yMap.remove(y);

                    if (tile != null && yMap.isEmpty()) {
                        tiles.remove(x);
                    }
                }

                if (tile != null) {
                    Map<Integer, TileInstance> yMap = tiles.computeIfAbsent(x, k -> new HashMap<>());
                    yMap.put(y, createTileInstance(x, y, tile));
                } else {
                    modify(x, y);
                }

                updateFill(startX, startY, endX, endY, x, y);
            }
        }

        if (tile != null) {
            for (int x = startX; x <= endX; x++) {
                for (int y = startY; y <= endY; y++) {
                    updateTile(x, y);
                    updateFill(startX, startY, endX, endY, x, y);
                }
            }
        }
    }

    public void fill(int startX, int startY, Tile[][] tilesArray) {
        for (int row = 0; row < tilesArray.length; row++) {
            for (int col = 0; col < tilesArray[row].length; col++) {

                int x = startX + row;
                int y = startY + col;
                Tile tile = tilesArray[row][col];

                if (contains(x, y)) {
                    Map<Integer, TileInstance> yMap = tiles.get(x);
                    scene().destroy(yMap.get(y).entity());
                    yMap.remove(y);
                    if (tile != null && yMap.isEmpty()) {
                        tiles.remove(x);
                    }
                }

                if (tile != null) {
                    Map<Integer, TileInstance> yMap = tiles.computeIfAbsent(x, k -> new HashMap<>());
                    yMap.put(y, createTileInstance(x, y, tile));
                } else {
                    modify(x, y);
                }

                updateFill(startX, startY, startX + tilesArray.length, startY + tilesArray[row].length, x, y);
            }
        }

        for (int row = 0; row < tilesArray.length; row++) {
            for (int col = 0; col < tilesArray[row].length; col++) {
                int x = startX + row;
                int y = startY + col;

                updateTile(x, y);
                updateFill(startX, startY, startX + tilesArray.length, startY + tilesArray[row].length, x, y);
            }
        }
    }

    private void updateFill(int startX, int startY, int endX, int endY, int x, int y) {
        if (x == startX) updateTile(startX - 1, y);
        if (x == endX) updateTile(endX + 1, y);

        if (y == startY) updateTile(x, startY - 1);
        if (y == endY) updateTile(x, endY + 1);

        if (x == startX && y == startY) updateTile(startX - 1, startY - 1);
        if (x == endX && y == endY) updateTile(endX + 1, endY + 1);
    }

    public void updateTile(int x, int y) {
        TileInstance instance = getTileInstance(x, y);
        if (instance != null) {
            instance.update();
            modify(x, y);
        }
    }

    @Override
    public Class<? extends Component> type() {
        return TileMap.class;
    }

    @Override
    protected Component self() {
        return this;
    }

    public boolean modified() {
        assertNotDeleted();
        return modified;
    }

    private void modify(int x, int y) {
        modified = true;
        updatedTiles.add(new Vector2i(x, y));
    }

    public List<Vector2i> updatedTiles() {
        return updatedTiles;
    }

    public int layerOrder() {
        return layerOrder;
    }

    public float tileSize() {
        return tileSize;
    }

    public Map<Integer, Map<Integer, TileInstance>> tiles() {
        return tiles;
    }

    public boolean useHardShadow() {
        return useHardShadow;
    }

    public TileMap useHardShadow(boolean useHardShadow) {
        this.useHardShadow = useHardShadow;
        return this;
    }
}

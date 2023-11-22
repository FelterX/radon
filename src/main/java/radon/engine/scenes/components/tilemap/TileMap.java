package radon.engine.scenes.components.tilemap;

import org.joml.Vector2f;
import org.joml.Vector2i;
import radon.engine.scenes.Component;
import radon.engine.scenes.components.behaviours.AbstractBehaviour;
import radon.engine.tiles.Tile;
import radon.engine.tiles.TileInstance;

import java.util.*;

public class TileMap extends Component {

    private Map<Integer, Map<Integer, TileInstance>> tiles = new HashMap<>();
    private int layerOrder = 0;
    private float tileSize = 1 / 16f;
    private boolean modified = false;
    private List<Vector2i> updatedTiles = new ArrayList<>();


    /*Map<Integer, Map<Integer, Tile>> tiles = tileMap.tiles();
   for (Map.Entry<Integer, Map<Integer, Tile>> entryX : tiles.entrySet()) {
       int x = entryX.getKey();
       Map<Integer, Tile> yMap = entryX.getValue();
       for (Map.Entry<Integer, Tile> entryY : yMap.entrySet()) {
           int y = entryY.getKey();
           Tile tile = entryY.getValue();


       }
   }*/
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
        yMap.put(y, new TileInstance(this, tile, x, y));

        for (int i = -1; i < 2; i++)
            for (int j = -1; j < 2; j++)
                updateTile(x + i, y + j);



        modify(x, y);
    }

    public void updateTile(int x, int y) {
        TileInstance instance = getTileInstance(x,y);
        if(instance != null) {
            instance.update();
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
}

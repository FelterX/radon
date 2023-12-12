package radon.engine.tiles.autotiling;

import radon.engine.scenes.components.tilemap.TileMap;
import radon.engine.sprites.Sprite;
import radon.engine.tiles.Tile;
import radon.engine.tiles.TileInstance;
import radon.engine.tiles.properties.TileProperty;

import java.util.HashMap;
import java.util.Map;

public class AutoTillingProcessor {

    private static final Map<Integer, Integer> INDICES = new HashMap<>();

    static {
        String autoTilingIndexStr = "2 = 1, 8 = 2, 10 = 3, 11 = 4, 16 = 5, 18 = 6, 22 = 7, 24 = 8, 26 = 9, 27 = 10, 30 = 11, 31 = 12, 64 = 13, 66 = 14, 72 = 15, 74 = 16, 75 = 17, 80 = 18, 82 = 19, 86 = 20, 88 = 21, 90 = 22, 91 = 23, 94 = 24, 95 = 25, 104 = 26, 106 = 27, 107 = 28, 120 = 29, 122 = 30, 123 = 31, 126 = 32, 127 = 33, 208 = 34, 210 = 35, 214 = 36, 216 = 37, 218 = 38, 219 = 39, 222 = 40, 223 = 41, 248 = 42, 250 = 43, 251 = 44, 254 = 45, 255 = 46";
        String[] split = autoTilingIndexStr.split(",", -1);
        for (String value : split) {
            String[] keyValue = value.split("=",2);
            Integer s = Integer.parseInt(keyValue[0].trim());
            Integer e = Integer.parseInt(keyValue[1].trim());

            INDICES.put(s, e);
        }
    }

    public static void process(TileMap tileMap, TileInstance tileI) {

        Tile tile, tileNW, tileN, tileNE, tileW, tileE, tileSW, tileS, tileSE;

        int x = tileI.x();
        int y = tileI.y();

        tile = tileI.tile();
        tileNW = tileMap.getTile(x - 1, y + 1);
        tileN = tileMap.getTile(x, y + 1);
        tileNE = tileMap.getTile(x + 1, y + 1);

        tileW = tileMap.getTile(x - 1, y);
        tileE = tileMap.getTile(x + 1, y);

        tileSW = tileMap.getTile(x - 1, y - 1);
        tileS = tileMap.getTile(x, y - 1);
        tileSE = tileMap.getTile(x + 1, y - 1);

        int i;

        boolean N = detectSameTileType(tile, tileN);
        boolean S = detectSameTileType(tile, tileS);
        boolean E = detectSameTileType(tile, tileE);
        boolean W = detectSameTileType(tile, tileW);

        boolean NW = detectSameTileType(tile, tileNW) && N && W;
        boolean NE = detectSameTileType(tile, tileNE) && N && E;
        boolean SW = detectSameTileType(tile, tileSW) && S && W;
        boolean SE = detectSameTileType(tile, tileSE) && S && E;


        i = (boolToInt(NW) << 0) |
                (boolToInt(N) << 1) |
                (boolToInt(NE) << 2) |
                (boolToInt(W) << 3) |
                (boolToInt(E) << 4) |
                (boolToInt(SW) << 5) |
                (boolToInt(S) << 6) |
                (boolToInt(SE) << 7);

        i = INDICES.getOrDefault(i, 0);

        Map<Integer, Integer> neighbors = new HashMap<>();

        if (tileN != null) neighbors.compute(tileN.id(), (k, v) -> v == null ? 1 : v + 1);
        if (tileS != null) neighbors.compute(tileS.id(), (k, v) -> v == null ? 1 : v + 1);
        if (tileE != null) neighbors.compute(tileE.id(), (k, v) -> v == null ? 1 : v + 1);
        if (tileW != null) neighbors.compute(tileW.id(), (k, v) -> v == null ? 1 : v + 1);
        if (tileNE != null) neighbors.compute(tileNE.id(), (k, v) -> v == null ? 1 : v + 1);
        if (tileNW != null) neighbors.compute(tileNW.id(), (k, v) -> v == null ? 1 : v + 1);
        if (tileSE != null) neighbors.compute(tileSE.id(), (k, v) -> v == null ? 1 : v + 1);
        if (tileSW != null) neighbors.compute(tileSW.id(), (k, v) -> v == null ? 1 : v + 1);

        int n = 0;
        int nums = 0;

        for (Map.Entry<Integer, Integer> entry : neighbors.entrySet()) {
            int key = entry.getKey();
            int value = entry.getValue();
            if (key == tile.id()) continue;
            n = (value > nums) ? key : n;
            nums = (value > nums) ? value : nums;
        }


        Sprite[] sprites = tileI.tile().getPropertyValue(TileProperty.AUTO_TILING);

        tileI.sprite(sprites[i]/*, i */);
    }

    private static boolean detectSameTileType(Tile a, Tile b) {
        return b != null && (a.id() == b.id());
    }

    private static int boolToInt(boolean b) {
        return b ? 1 : 0;
    }
}

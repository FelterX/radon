package radon.engine.graphics.opengl.rendering.renderers;

import org.joml.Vector2f;
import org.joml.Vector2i;
import radon.engine.graphics.opengl.textures.GLTexture;
import radon.engine.logging.Log;
import radon.engine.scenes.components.math.Transform;
import radon.engine.scenes.components.sprites.SpriteInstance;
import radon.engine.scenes.components.tilemap.TileMap;
import radon.engine.sprites.Sprite;
import radon.engine.tiles.Tile;
import radon.engine.tiles.TileInstance;

import java.util.*;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;


public class GLTileMapBatch implements Comparable<GLTileMapBatch> {

    private static final int POS_SIZE = 2;
    private static final int COLOR_SIZE = 4;
    private static final int TEX_COORDS_SIZE = 2;
    private static final int TEX_ID_SIZE = 1;
    private static final int POS_OFFSET = 0;
    private static final int COLOR_OFFSET = POS_OFFSET + POS_SIZE * Float.BYTES;
    private static final int TEX_COORDS_OFFSET = COLOR_OFFSET + COLOR_SIZE * Float.BYTES;
    private static final int TEX_ID_OFFSET = TEX_COORDS_OFFSET + TEX_COORDS_SIZE * Float.BYTES;
    private static final int VERTEX_SIZE = 9;
    private static final int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;
    protected static final int[] TEXTURES_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7};

    private GLTileMapRenderer renderer;
    private TileMap tileMap;
    private int numSprites;
    private boolean hasRoom;
    private float[] vertices;
    private List<GLTexture> textures;
    private int vaoID, vboID;
    private int maxBatchSize;
    private Map<Integer, Map<Integer, Integer>> tileIndexMap;
    private Queue<Integer> emptyIndex;
    boolean rebufferData = true;

    protected GLTileMapBatch(GLTileMapRenderer renderer, TileMap tileMap, int maxBatchSize) {
        this.renderer = renderer;
        this.tileMap = tileMap;
        this.maxBatchSize = maxBatchSize;

        this.tileIndexMap = new HashMap<>();
        this.emptyIndex = new ArrayDeque<>();

        this.vertices = new float[maxBatchSize * 4 * VERTEX_SIZE];

        this.numSprites = 0;
        this.hasRoom = true;
        this.textures = new ArrayList<>();


        Map<Integer, Map<Integer, TileInstance>> tiles = tileMap.tiles();
        for (Map.Entry<Integer, Map<Integer, TileInstance>> entryX : tiles.entrySet()) {
            int x = entryX.getKey();
            Map<Integer, TileInstance> yMap = entryX.getValue();
            for (Map.Entry<Integer, TileInstance> entryY : yMap.entrySet()) {
                int y = entryY.getKey();
                TileInstance tile = entryY.getValue();
                addTile(x, y, tile);
            }
        }
    }

    private int generateId() {
        if (!emptyIndex.isEmpty())
            return emptyIndex.poll();

        return numSprites;
    }

    protected void init() {
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, vertices.length * Float.BYTES, GL_DYNAMIC_DRAW);

        int eboID = glGenBuffers();
        int[] indices = generateIndices();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, POS_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, POS_OFFSET);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, COLOR_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, COLOR_OFFSET);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, TEX_COORDS_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_COORDS_OFFSET);
        glEnableVertexAttribArray(2);

        glVertexAttribPointer(3, TEX_ID_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_ID_OFFSET);
        glEnableVertexAttribArray(3);

    }

    private int[] generateIndices() {
        int[] elements = new int[6 * maxBatchSize];
        for (int i = 0; i < maxBatchSize; i++) {
            loadElementIndices(elements, i);
        }

        return elements;
    }

    private void loadElementIndices(int[] elements, int index) {
        int offsetArrayIndex = 6 * index;
        int offset = 4 * index;

        // 3, 2, 0, 0, 2, 1        7, 6, 4, 4, 6, 5
        // Triangle 1
        elements[offsetArrayIndex] = offset + 3;
        elements[offsetArrayIndex + 1] = offset + 2;
        elements[offsetArrayIndex + 2] = offset;

        // Triangle 2
        elements[offsetArrayIndex + 3] = offset;
        elements[offsetArrayIndex + 4] = offset + 2;
        elements[offsetArrayIndex + 5] = offset + 1;
    }

    public void render() {

        if (tileMap.modified()) {
            List<Vector2i> updatedTile = tileMap.updatedTiles();
            for (Vector2i tilePos : updatedTile) {
                int x = tilePos.x;
                int y = tilePos.y;

                if (!tileMap.contains(tilePos.x, tilePos.y)) {
                    removeTile(x, y);
                } else {
                    if (getTileIndex(x, y) == -1)
                        addTile(x, y, tileMap.getTileInstance(x, y));

                    loadVertexProperties(tilePos.x, tilePos.y);
                }
                rebufferData = true;
            }
        }

        if (rebufferData) {
            glBindBuffer(GL_ARRAY_BUFFER, vboID);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
            rebufferData = false;
        }

        for (int i = 0; i < textures.size(); i++) {
            glActiveTexture(GL_TEXTURE0 + i + 1);
            glBindTexture(GL_TEXTURE_2D, textures.get(i).handle());
        }

        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawElements(GL_TRIANGLES, this.numSprites * 6, GL_UNSIGNED_INT, 0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);

        for (int i = 0; i < textures.size(); i++) {
            glBindTexture(GL_TEXTURE_2D, 0);
        }
    }

    private void loadVertexProperties(int x, int y) {
        TileInstance tile = tileMap.getTileInstance(x, y);
        if (tile == null) return;

        int index = getTileIndex(x, y);
        if (index == -1) return;

        Sprite sprite = tile.sprite();
        GLTexture texture = sprite.texture();

        int offset = index * 4 * VERTEX_SIZE;

        Vector2f[] textureCoords = sprite.textureCoords();

        int texId = 0;
        if (texture != null) {
            for (int i = 0; i < textures.size(); i++) {
                if (textures.get(i).equals(texture)) {
                    texId = i + 1;
                    break;
                }
            }
        }

        float xAdd = 0.5f;
        float yAdd = 0.5f;
        for (int i = 0; i < 4; i++) {
            if (i == 1) {
                yAdd = -0.5f;
            } else if (i == 2) {
                xAdd = -0.5f;
            } else if (i == 3) {
                yAdd = 0.5f;
            }

            // Load position
            vertices[offset] = (x + xAdd) * tileMap.tileSize();
            vertices[offset + 1] = (y + yAdd) * tileMap.tileSize();

            // Load color
            vertices[offset + 2] = 1.0f;
            vertices[offset + 3] = 1.0f;
            vertices[offset + 4] = 1.0f;
            vertices[offset + 5] = 1.0f;

            // Load texture coordinates
            vertices[offset + 6] = textureCoords[i].x;
            vertices[offset + 7] = textureCoords[i].y;

            // Load texture id
            vertices[offset + 8] = texId;

            offset += VERTEX_SIZE;
        }
    }

    private void addTile(int x, int y, TileInstance tile) {
        if (!contains(x, y)) {
            Sprite sprite = tile.sprite();
            if (!hasTexture(sprite.texture()))
                textures.add(sprite.texture());

            int id = generateId();
            setTileIndex(x, y, id);
            numSprites++;
        }
    }

    private void removeTile(int x, int y) {
        int index = getTileIndex(x, y);
        if (index == -1) return;

        int offset = index * 4 * VERTEX_SIZE;

        for (int i = 0; i < 4; i++) {
            // Load position
            vertices[offset] = 0;
            vertices[offset + 1] = 0;

            // Load color
            vertices[offset + 2] = 1.0f;
            vertices[offset + 3] = 1.0f;
            vertices[offset + 4] = 1.0f;
            vertices[offset + 5] = 1.0f;

            // Load texture coordinates
            vertices[offset + 6] = 0;
            vertices[offset + 7] = 0;

            // Load texture id
            vertices[offset + 8] = 0;

            offset += VERTEX_SIZE;
        }
        removeIndex(x, y);
        numSprites--;
    }

    public boolean contains(int x, int y) {
        Map<Integer, Integer> yMap = tileIndexMap.get(x);
        if (yMap == null) return false;
        return yMap.containsKey(y);
    }

    private int getTileIndex(int x, int y) {
        if (contains(x, y)) return tileIndexMap.get(x).get(y);
        return -1;
    }

    private void setTileIndex(int x, int y, int index) {
        Map<Integer, Integer> yMap = tileIndexMap.computeIfAbsent(x, k -> new HashMap<>());
        yMap.put(y, index);
    }

    private void removeIndex(int x, int y) {
        if (contains(x, y)) {
            int index = getTileIndex(x, y);

            Map<Integer, Integer> yMap = tileIndexMap.get(x);
            yMap.remove(y);
            if (yMap.isEmpty()) {
                tileIndexMap.remove(x);
            }

            emptyIndex.add(index);
        }
    }

    protected void terminate() {
        glDeleteBuffers(vboID);
        glDeleteVertexArrays(vaoID);
    }

    public boolean hasRoom() {
        return this.hasRoom;
    }

    public boolean hasTextureRoom() {
        return this.textures.size() < 7;
    }

    public boolean hasTexture(GLTexture texture) {
        return this.textures.contains(texture);
    }

    @Override
    public int compareTo(GLTileMapBatch o) {
        return Integer.compare(this.tileMap.layerOrder(), o.tileMap.layerOrder());
    }
}

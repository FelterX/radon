package radon.engine.graphics.opengl.rendering.renderers;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import radon.engine.graphics.opengl.shaders.GLShaderProgram;
import radon.engine.graphics.opengl.textures.GLTexture;
import radon.engine.logging.Log;
import radon.engine.scenes.Camera;
import radon.engine.scenes.Scene;
import radon.engine.scenes.components.math.Transform;
import radon.engine.scenes.components.sprites.SpriteInstance;
import radon.engine.scenes.components.sprites.SpriteInstanceManager;
import radon.engine.sprites.Sprite;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;


public class GLSpriteBatch implements Comparable<GLSpriteBatch> {

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

    private GLSpriteRenderer renderer;
    private SpriteInstance[] sprites;
    private int numSprites;
    private boolean hasRoom;
    private float[] vertices;
    private List<GLTexture> textures;
    private int vaoID, vboID;
    private int maxBatchSize;
    private int layerOrder;
    private boolean rebufferData = true;

    protected GLSpriteBatch(GLSpriteRenderer renderer, int maxBatchSize, int layerOrder) {
        this.renderer = renderer;
        this.maxBatchSize = maxBatchSize;
        this.layerOrder = layerOrder;
        this.sprites = new SpriteInstance[maxBatchSize];

        this.vertices = new float[maxBatchSize * 4 * VERTEX_SIZE];

        this.numSprites = 0;
        this.hasRoom = true;
        this.textures = new ArrayList<>();
    }

    protected void init() {
        // Generate and bind a Vertex Array Object
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        // Allocate space for vertices
        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, vertices.length * Float.BYTES, GL_DYNAMIC_DRAW);

        // Create and upload indices buffer
        int eboID = glGenBuffers();
        int[] indices = generateIndices();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        // Enable the buffer attribute pointers
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
        // 6 indices per quad (3 per triangle)
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

    public void addSprite(SpriteInstance instance) {
        Sprite sprite = instance.sprite();

        int index = this.numSprites;
        this.sprites[index] = instance;
        this.numSprites++;

        if (sprite.texture() != null) {
            if (!textures.contains(sprite.texture())) {
                textures.add(sprite.texture());
            }
        }

        loadVertexProperties(index);

        if (numSprites >= this.maxBatchSize) {
            this.hasRoom = false;
        }
    }

    public boolean removeSprite(SpriteInstance instance) {
        for (int i = 0; i < numSprites; i++) {
            if (sprites[i] == instance) {
                for (int j = i; j < numSprites - 1; j++) {
                    sprites[j] = sprites[j + 1];
                }
                numSprites--;
                rebufferData = true;
                return true;
            }
        }

        return false;
    }

    public void render() {
        for (int i = 0; i < numSprites; i++) {
            SpriteInstance instance = sprites[i];
            Transform transform = (Transform) instance.get(Transform.class);
            Sprite sprite = instance.sprite();
            if (instance.modified() || transform.modified()) {
                if (!hasTexture(sprite.texture())) {
                    this.renderer.remove(instance);
                    this.renderer.add(instance);
                } else {
                    loadVertexProperties(i);
                    rebufferData = true;
                }
            }

            if (instance.layerOrder() != this.layerOrder) {
                removeSprite(instance);
                renderer.add(instance);
                i--;
            }
        }
        if (rebufferData) {
            glBindBuffer(GL_ARRAY_BUFFER, vboID);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
            rebufferData = false;
            Log.info("Rebuffer Data");
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

    private void loadVertexProperties(int index) {
        SpriteInstance instance = this.sprites[index];
        Sprite sprite = instance.sprite();
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

        Transform transform = (Transform) instance.get(Transform.class);
        Vector3fc position = transform.position();
        Vector3fc scale = transform.scale();

        // Add vertices with the appropriate properties
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

            Vector4f currentPos = new Vector4f(
                    position.x() + (xAdd * scale.x()),
                    position.y() + (yAdd * scale.y()),
                    0, 1);

            // Load position
            vertices[offset] = currentPos.x;
            vertices[offset + 1] = currentPos.y;

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

    public int layerOrder() {
        return this.layerOrder;
    }

    @Override
    public int compareTo(GLSpriteBatch o) {
        return Integer.compare(this.layerOrder, o.layerOrder());
    }
}

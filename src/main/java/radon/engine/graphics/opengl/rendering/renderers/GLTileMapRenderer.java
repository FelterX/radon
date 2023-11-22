package radon.engine.graphics.opengl.rendering.renderers;

import radon.engine.core.RadonFiles;
import radon.engine.graphics.opengl.GLContext;
import radon.engine.graphics.opengl.shaders.GLShader;
import radon.engine.graphics.opengl.shaders.GLShaderProgram;
import radon.engine.graphics.opengl.textures.GLTexture;
import radon.engine.logging.Log;
import radon.engine.scenes.Camera;
import radon.engine.scenes.Scene;
import radon.engine.scenes.components.sprites.SpriteInstance;
import radon.engine.scenes.components.tilemap.TileMap;
import radon.engine.scenes.components.tilemap.TileMapManager;
import radon.engine.sprites.SceneSpriteInfo;
import radon.engine.sprites.Sprite;
import radon.engine.tiles.SceneTileMapInfo;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static radon.engine.graphics.ShaderStage.FRAGMENT_STAGE;
import static radon.engine.graphics.ShaderStage.VERTEX_STAGE;
import static radon.engine.graphics.opengl.rendering.renderers.GLSpriteBatch.TEXTURES_SLOTS;

public class GLTileMapRenderer extends GLRenderer {

    public static final Path TILE_VERTEX_SHADER_PATH = RadonFiles.getPath("shaders/tile/tile.vert");
    public static final Path TILE_FRAGMENT_SHADER_PATH = RadonFiles.getPath("shaders/tile/tile.frag");
    private static final int MAX_BATCH_SIZE = 6144;

    private final List<GLTileMapBatch> batches;
    private GLShaderProgram shader;


    public GLTileMapRenderer(GLContext context) {
        super(context);
        batches = new ArrayList<>();
    }

    @Override
    public void init() {
        this.shader = createShader();
    }

    public void add(TileMap tileMap) {

        GLTileMapBatch newBatch = new GLTileMapBatch(this, tileMap, MAX_BATCH_SIZE);
        newBatch.init();
        batches.add(newBatch);
        Collections.sort(batches);
        Log.info("Batch added");
    }

    public void render(Scene scene) {
        SceneTileMapInfo tileMapInfo = scene.tileMapInfo();
        boolean added = false;

        for (TileMap map : tileMapInfo.newMaps()) {
            add(map);
            added = true;
        }
        if (added) {
            tileMapInfo.onAdded();
        }

        Camera camera = scene.camera();
        shader.bind();
        shader.uniformMatrix4f("uProjection", false, camera.projectionMatrix());
        shader.uniformMatrix4f("uView", false, camera.viewMatrix());
        shader.uniformArrayInt("uTextures", GLTileMapBatch.TEXTURES_SLOTS);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        for (GLTileMapBatch batch : batches) {
            batch.render();
        }

        glDisable(GL_BLEND);

        shader.unbind();

    }

    @Override
    public void terminate() {
        this.shader.deleteShaders();
    }


    private GLShaderProgram createShader() {
        return new GLShaderProgram(context(), "OpenGL Tile shader")
                .attach(new GLShader(context(), VERTEX_STAGE).source(TILE_VERTEX_SHADER_PATH).compile())
                .attach(new GLShader(context(), FRAGMENT_STAGE).source(TILE_FRAGMENT_SHADER_PATH).compile())
                .link();
    }
}

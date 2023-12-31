package radon.examples;

import org.joml.Vector2i;
import org.joml.Vector3f;
import radon.engine.core.Radon;
import radon.engine.core.RadonApplication;
import radon.engine.core.RadonFiles;
import radon.engine.graphics.GraphicsFactory;
import radon.engine.graphics.anim.Animation;
import radon.engine.graphics.anim.keys.AnimationSpriteKey;
import radon.engine.graphics.opengl.textures.GLTexture2D;
import radon.engine.graphics.textures.Texture;
import radon.engine.images.PixelFormat;
import radon.engine.input.Input;
import radon.engine.input.Key;
import radon.engine.scenes.Camera;
import radon.engine.scenes.Entity;
import radon.engine.scenes.Scene;
import radon.engine.scenes.SceneManager;
import radon.engine.scenes.components.anim.AnimationPlayer;
import radon.engine.scenes.components.math.Transform;
import radon.engine.scenes.components.sprites.SpriteInstance;
import radon.engine.scenes.components.tilemap.TileMap;
import radon.engine.sprites.Sprite;
import radon.engine.tiles.MultiTile;
import radon.engine.tiles.Tile;
import radon.engine.tiles.autotiling.AutoTile;
import radon.engine.tiles.autotiling.AutoTillingGenerator;
import radon.engine.util.geometry.Rect;

import java.util.Random;

import static radon.engine.util.Maths.radians;

public class SpriteExample extends RadonApplication {

    public static void main(String[] args) {
        Radon.launch(new SpriteExample());
    }

    TileMap tileMap;
    AutoTile tile;

    private SpriteExample() {

    }

    @Override
    protected void onStart(Scene scene) {

        scene.camera().projectionType(Camera.ProjectionType.ORTHOGRAPHIC);
        scene.camera().position(0, 0, 10);

        GLTexture2D texture = (GLTexture2D) GraphicsFactory.get().newTexture2D(RadonFiles.getPath("examples/chibi-layered.png"), PixelFormat.RGBA).setQuality(Texture.Quality.LOW);
        Sprite sprite00 = new Sprite(texture, new Rect(0, 16, 0, 16));
        Sprite sprite10 = new Sprite(texture, new Rect(16, 32, 0, 16));
        Sprite sprite20 = new Sprite(texture, new Rect(32, 48, 0, 16));
        Sprite sprite30 = new Sprite(texture, new Rect(48, 64, 0, 16));
        Sprite sprite40 = new Sprite(texture, new Rect(64, 80, 0, 16));
        Sprite sprite50 = new Sprite(texture, new Rect(80, 96, 0, 16));
        Sprite sprite60 = new Sprite(texture, new Rect(96, 112, 0, 16));
        Sprite sprite70 = new Sprite(texture, new Rect(112, 128, 0, 16));
        Sprite sprite80 = new Sprite(texture, new Rect(128, 144, 0, 16));


        Entity e = scene.newEntity("Player");
        e.add(SpriteInstance.class).sprite(sprite00);
        e.get(Transform.class).position(0, 0, 0);
        e.get(Transform.class).scale(1 / 16f);
        e.get(Transform.class).rotateY(radians(180.0f));
        e.add(SpriteBehaviour.class);


        Animation animation = new Animation();
        try {
            animation.addKey(new AnimationSpriteKey(0, e.get(SpriteInstance.class), sprite00));
            animation.addKey(new AnimationSpriteKey(0.1f, e.get(SpriteInstance.class), sprite30));
            animation.addKey(new AnimationSpriteKey(0.2f, e.get(SpriteInstance.class), sprite60));
            animation.addKey(new AnimationSpriteKey(0.3f, e.get(SpriteInstance.class), sprite00));

        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }

        e.add(AnimationPlayer.class).playAnimation(animation);

        animation.loop(true);

        Entity tileMapEntity = scene.newEntity("tileMapEntity");
        tileMap = tileMapEntity.add(TileMap.class);

        GLTexture2D spriteSheet = (GLTexture2D) GraphicsFactory.get().newTexture2D(RadonFiles.getPath("examples/sheet_dev.png"), PixelFormat.RGBA).setQuality(Texture.Quality.LOW);
        Sprite[] sprites = AutoTillingGenerator.generateSpriteSheet(spriteSheet, 16, 16);
        tile = new AutoTile(0, "tile", sprites);

        GLTexture2D testMultiTile = (GLTexture2D) GraphicsFactory.get().newTexture2D(RadonFiles.getPath("examples/multi_tile.png"), PixelFormat.RGBA).setQuality(Texture.Quality.LOW);
        Sprite sprite = new Sprite(testMultiTile);
        MultiTile multiTile = new MultiTile(1, "multiTile", sprite, new Vector2i(1, 3));
        Tile[][] tiles = new Tile[][]
                {
                        {tile, tile, tile},
                        {tile, tile, tile},
                        {tile, tile, tile}
                };

        tileMap.fill(0, 0, tiles);

        Entity tileMapEntity2 = scene.newEntity("tileMapEntity2");
        TileMap tileMap2 = tileMapEntity2.add(TileMap.class).useHardShadow(true);

        tileMap2.setTile(0, 0, multiTile);
    }

    @Override
    protected void onUpdate() {
        super.onUpdate();

        Vector3f pos = (Vector3f) SceneManager.scene().entity("Player").get(Transform.class).position();

        SceneManager.scene().camera().position(pos.x, pos.y, 10);
    }
}

package radon.examples;

import radon.engine.core.Radon;
import radon.engine.core.RadonApplication;
import radon.engine.core.RadonConfiguration;
import radon.engine.core.RadonFiles;
import radon.engine.graphics.GraphicsFactory;
import radon.engine.graphics.opengl.textures.GLTexture2D;
import radon.engine.graphics.textures.Texture;
import radon.engine.images.PixelFormat;
import radon.engine.scenes.Camera;
import radon.engine.scenes.Entity;
import radon.engine.scenes.Scene;
import radon.engine.scenes.SceneManager;
import radon.engine.scenes.components.math.Transform;
import radon.engine.scenes.components.sprites.SpriteInstance;
import radon.engine.sprites.Sprite;
import radon.engine.util.geometry.Rect;

public class SpriteExample extends RadonApplication {

    public static void main(String[] args) {
        RadonConfiguration.VSYNC.set(false);
        Radon.launch(new SpriteExample());
    }

    private SpriteExample() {

    }

    @Override
    protected void onStart(Scene scene) {

        scene.camera().projectionType(Camera.ProjectionType.ORTHOGRAPHIC);
        scene.camera().position(0, 0, 10);

        GLTexture2D texture = (GLTexture2D) GraphicsFactory.get().newTexture2D(RadonFiles.getPath("textures/examples/chibi-layered.png"), PixelFormat.RGBA).setQuality(Texture.Quality.LOW);
        Sprite sprite0 = new Sprite(texture, new Rect(0, 16, 0, 16));
        Sprite sprite1 = new Sprite(texture, new Rect(16, 32, 0, 16));

        float scale = 1 / 24f;
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                Entity e = scene.newEntity("Sprite" + i + "_" + j);
                e.add(SpriteInstance.class).sprite(sprite0, 0);
                e.get(Transform.class).position(i * scale, j * scale, 0);
                e.get(Transform.class).scale(scale);
            }
        }
    }
}

package radon.examples;

import radon.engine.core.Radon;
import radon.engine.core.RadonApplication;
import radon.engine.core.RadonFiles;
import radon.engine.graphics.GraphicsFactory;
import radon.engine.graphics.anim.Animation;
import radon.engine.graphics.anim.keys.AnimationMethodKey;
import radon.engine.graphics.anim.keys.AnimationSpriteKey;
import radon.engine.graphics.opengl.textures.GLTexture2D;
import radon.engine.graphics.textures.Texture;
import radon.engine.images.PixelFormat;
import radon.engine.scenes.Camera;
import radon.engine.scenes.Entity;
import radon.engine.scenes.Scene;
import radon.engine.scenes.components.anim.AnimationPlayer;
import radon.engine.scenes.components.math.Transform;
import radon.engine.scenes.components.sprites.SpriteInstance;
import radon.engine.sprites.Sprite;
import radon.engine.util.geometry.Rect;

import java.lang.reflect.Method;

public class SpriteExample extends RadonApplication {

    public static void main(String[] args) {
        Radon.launch(new SpriteExample());
    }

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


        Entity e = scene.newEntity();
        e.add(SpriteInstance.class).sprite(sprite00);
        e.get(Transform.class).position(0, 0, 0);
        e.get(Transform.class).scale(0.2f);
        e.add(SpriteBehaviour.class);


        Animation animation = new Animation();
        try {
            animation.addKey(new AnimationSpriteKey(0, e.get(SpriteInstance.class) , sprite00));
            animation.addKey(new AnimationSpriteKey(0.1f, e.get(SpriteInstance.class), sprite30));
            animation.addKey(new AnimationSpriteKey(0.2f, e.get(SpriteInstance.class), sprite60));
            animation.addKey(new AnimationSpriteKey(0.3f, e.get(SpriteInstance.class), sprite00));

        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }

        e.add(AnimationPlayer.class).playAnimation(animation);

        animation.loop(true);
    }
}

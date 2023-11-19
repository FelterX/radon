package radon.engine.graphics.anim.keys;

import radon.engine.scenes.components.sprites.SpriteInstance;
import radon.engine.sprites.Sprite;

public class AnimationSpriteKey extends AnimationMethodKey {

    public AnimationSpriteKey(float time, SpriteInstance instance, Sprite newSprite) throws NoSuchMethodException {
        super(time, instance, SpriteInstance.class.getDeclaredMethod("sprite", Sprite.class), newSprite);
    }
}

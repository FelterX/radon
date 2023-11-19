package radon.engine.scenes.components.anim;

import radon.engine.graphics.anim.Animation;
import radon.engine.graphics.anim.keys.AnimationKey;
import radon.engine.scenes.components.behaviours.Behaviour;

import static radon.engine.core.Time.deltaTime;

public class AnimationPlayer extends Behaviour {
    private Animation currentAnimation = null;
    private int currentKey;
    private float time;

    @Override
    public void onUpdate() {
        if (currentAnimation != null) {
            time += deltaTime();

            if (currentKey < currentAnimation.numKeys()) {
                if (currentKey == -1) {
                    AnimationKey key = currentAnimation.key(0);
                    if (time >= key.time()) {
                        key.execute();
                        currentKey = 0;
                    }
                } else {
                    AnimationKey key = currentAnimation.key(currentKey + 1);
                    if (time >= key.time()) {
                        key.execute();
                        currentKey++;
                    }
                }
            }

            if (time >= currentAnimation.duration()) {
                if (currentAnimation.isLoop()) {
                    time = 0;
                    currentKey = -1;
                } else {
                    currentAnimation = null;
                }
            }
        }
    }

    public Animation currentAnimation() {
        return currentAnimation;
    }

    public void playAnimation(Animation currentAnimation) {
        if (this.currentAnimation != currentAnimation) {
            this.currentAnimation = currentAnimation;
            time = 0;
            currentKey = -1;
        }
    }
}

package radon.engine.graphics.anim;

import radon.engine.graphics.anim.keys.AnimationKey;
import radon.engine.logging.Log;

import java.util.*;

public class Animation {

    protected List<AnimationKey> keys = new ArrayList<>();
    protected boolean isLoop = false;
    private float duration = -1.0f;


    public Animation addKey(AnimationKey key) {
        for (AnimationKey k : keys) {
            if (k.time() == key.time()) {
                Log.error("AnimationKey already set at : " + key.time());
                return this;
            }
        }

        keys.add(key);

        if (key.time() > duration) duration = key.time();

        Collections.sort(keys);
        return this;
    }

    public boolean isLoop() {
        return isLoop;
    }

    public Animation loop(boolean loop) {
        isLoop = loop;
        return this;
    }

    public float duration() {
        return duration;
    }

    public AnimationKey key(int index) {
        return keys.get(index);
    }

    public int numKeys() {
        return keys.size();
    }
}

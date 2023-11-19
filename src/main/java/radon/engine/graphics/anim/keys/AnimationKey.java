package radon.engine.graphics.anim.keys;


public abstract class AnimationKey implements Comparable<AnimationKey> {

    private final float time;

    public AnimationKey(float time) {
        this.time = time;
    }

    public abstract void execute();

    @Override
    public int compareTo(AnimationKey o) {
        return Float.compare(time, o.time);
    }

    public float time() {
        return time;
    }
}

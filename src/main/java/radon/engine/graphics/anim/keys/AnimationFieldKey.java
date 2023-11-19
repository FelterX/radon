package radon.engine.graphics.anim.keys;

import java.lang.reflect.Field;

import static java.util.Objects.requireNonNull;

public class AnimationFieldKey extends AnimationKey {

    protected Object object;
    protected Field field;
    protected Object value;

    public AnimationFieldKey(float time, Object object, Field field, Object value) {
        super(time);

        this.object = object;
        this.field = requireNonNull(field);
        this.value = value;
    }

    @Override
    public void execute() {
        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}

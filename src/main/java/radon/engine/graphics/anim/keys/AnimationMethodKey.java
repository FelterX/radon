package radon.engine.graphics.anim.keys;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.util.Objects.requireNonNull;

public class AnimationMethodKey extends AnimationKey {

    protected Object object;
    protected Method method;
    protected Object[] args = new Object[0];

    public AnimationMethodKey(float time, Object object, Method methode, Object... args) {
        super(time);

        this.object = object;
        this.method = requireNonNull(methode);
        this.args = args;
    }

    @Override
    public void execute() {
        try {
            method.invoke(object, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}

package radon.engine.util;

import radon.engine.scenes.Component;

import java.lang.reflect.Field;
import java.util.Objects;

public interface TriConsumer<T, U, V> {

    void accept(T t, Component u, Field v);

    default TriConsumer<T, U, V> andThen(TriConsumer<? super T, ? super U, ? super V> after) {
        Objects.requireNonNull(after);

        return (l, r, o) -> {
            accept(l, r, o);
            after.accept(l, r, o);
        };
    }
}

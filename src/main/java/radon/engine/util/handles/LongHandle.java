package radon.engine.util.handles;

public interface LongHandle {

    long NULL = 0;

    long handle();

    default boolean isNull() {
        return handle() == NULL;
    }
}

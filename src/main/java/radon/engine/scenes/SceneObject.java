package radon.engine.scenes;

import radon.engine.logging.Log;

import static radon.engine.core.RadonConfigConstants.ENABLE_ASSERTS;

public abstract class SceneObject {

    private static final byte MARK_DESTROYED = 1;
    private static final byte DELETED = 2;

    private byte destroyState;

    void init() {
        destroyState = 0;
    }

    public abstract Scene scene();

    public abstract boolean enabled();

    public final SceneObject enable(boolean enable) {
        return enable ? enable() : disable();
    }

    public abstract SceneObject enable();

    public abstract SceneObject disable();

    public boolean destroyed() {
        return destroyState == MARK_DESTROYED;
    }

    public boolean deleted() {
        return destroyState == DELETED;
    }

    void delete() {
        onDestroy();
        destroyState = DELETED;
    }

    final void markDestroyed() {
        destroyState = MARK_DESTROYED;
    }

    public abstract void destroy();

    public abstract void destroyNow();

    protected abstract void onDestroy();

    protected final void doLater(Runnable task) {
        assertNotDeleted();
        scene().submit(task);
    }

    protected final void assertNotDeleted() {
        if(ENABLE_ASSERTS) {
            if(deleted()) {
                Log.fatal("SceneObject " + toString() + " is deleted");
            }
        }
    }
}

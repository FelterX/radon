package radon.engine.core;

public abstract class RadonSystem {
    private final RadonSystemManager systemManager;
    private boolean initialized;

    public RadonSystem(RadonSystemManager systemManager) {
        this.systemManager = systemManager;
    }

    protected final RadonSystemManager getSystemManager() {
        return systemManager;
    }

    final void markInitialized() {
        initialized = true;
    }

    public final boolean initialized() {
        return initialized;
    }

    protected abstract void init();

    protected abstract void terminate();

    protected CharSequence debugReport() {
        return null;
    }
}

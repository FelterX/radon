package radon.engine.core;

import radon.engine.logging.Log;
import radon.engine.scenes.Scene;
import radon.engine.util.types.Singleton;

public abstract class RadonApplication {
    @Singleton
    private static RadonApplication instance;
    private volatile boolean running;

    public RadonApplication() {
    }

    public static void exit() {
        instance.running = false;
    }

    public final boolean running() {
        return running;
    }

    protected void onInit() {

    }

    protected abstract void onStart(Scene scene);

    protected void onUpdate() {

    }

    protected void onRenderBegin() {

    }

    protected void onRenderEnd() {

    }

    protected void onError(Throwable error) {
        Log.error("An unexpected error has crashed the Application" + errorMessage(error), error);
    }

    private String errorMessage(Throwable error) {
        String message = error.getMessage();
        return message == null ? "" : message;
    }

    protected void onTerminate() {

    }

    void start(Scene scene) {
        running = true;
        onStart(scene);
    }
}

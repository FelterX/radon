package radon.engine.graphics;

import radon.engine.core.RadonSystem;
import radon.engine.core.RadonSystemManager;
import radon.engine.graphics.opengl.GLContext;
import radon.engine.graphics.window.Window;
import radon.engine.graphics.window.WindowFactory;
import radon.engine.logging.Log;
import radon.engine.util.types.Singleton;

import static java.util.Objects.requireNonNull;
import static radon.engine.core.RadonConfigConstants.GRAPHICS_API;
import static radon.engine.graphics.GraphicsAPI.OPENGL;
import static radon.engine.util.types.TypeUtils.initSingleton;
import static radon.engine.util.types.TypeUtils.newInstance;

public final class Graphics extends RadonSystem {

    @Singleton
    private static Graphics instance;

    private static Thread graphicsThread;

    public static GraphicsContext graphicsContext() {
        return instance.graphicsContext;
    }

    public static boolean isGraphicsThread() {
        return Thread.currentThread() == graphicsThread;
    }


    private GraphicsContext graphicsContext;
    private Window window;

    private Graphics(RadonSystemManager systemManager) {
        super(systemManager);
    }

    @Override
    protected void init() {

        graphicsThread = Thread.currentThread();

        GraphicsAPI chosenGraphicsAPI = GRAPHICS_API;

        if(chosenGraphicsAPI != OPENGL) {
            Log.fatal("Radon does not support " + chosenGraphicsAPI + " at the moment. Use OPENGL instead");
            return;
        }

        initSingleton(GraphicsAPI.class, chosenGraphicsAPI);

        Log.info("Using " + chosenGraphicsAPI + " as the Graphics API");

        Log.info("Creating window...");

        window = requireNonNull(newInstance(WindowFactory.class)).newWindow();

        Log.info("Window created");

        Log.info("Creating Graphics Context...");

        graphicsContext = createGraphicsContext();
        graphicsContext.init();

        Log.info(GraphicsAPI.get()  + " Context created");
    }

    private GraphicsContext createGraphicsContext() {
        return newInstance(GLContext.class);
    }

    @Override
    protected void terminate() {
        if(graphicsContext != null) {
            graphicsContext.release();
        }
        window.destroy();
    }

}

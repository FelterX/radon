package radon.engine.core;

import radon.engine.assets.AssetsSystem;
import radon.engine.audio.AudioSystem;
import radon.engine.events.EventManager;
import radon.engine.graphics.Graphics;
import radon.engine.graphics.rendering.RenderSystem;
import radon.engine.input.Input;
import radon.engine.logging.Log;
import radon.engine.resource.ResourceManager;
import radon.engine.scenes.SceneManager;
import radon.engine.tasks.TaskManager;
import radon.engine.util.types.TypeUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

import static radon.engine.util.types.TypeUtils.newInstance;

public class RadonSystemManager {

    private final Log log;
    private final GLFWLibrary glfwLibrary;
    private final Time time;
    private final EventManager eventManager;
    private final Input input;
    private final Graphics graphics;
    private final ResourceManager resourceManager;
    private final AssetsSystem assetsSystem;
    private final RenderSystem renderSystem;
    private final AudioSystem audioSystem;
    private final TaskManager taskManager;
    private final SceneManager sceneManager;
    // All systems in array for sorted initialization / termination
    private final RadonSystem[] systems;

    public RadonSystemManager() {
        systems = new RadonSystem[]{
                log = createSystem(Log.class),
                glfwLibrary = createSystem(GLFWLibrary.class),
                time = createSystem(Time.class),
                eventManager = createSystem(EventManager.class),
                resourceManager = createSystem(ResourceManager.class),
                graphics = createSystem(Graphics.class),
                input = createSystem(Input.class),
                audioSystem = createSystem(AudioSystem.class),
                assetsSystem = createSystem(AssetsSystem.class),
                renderSystem = createSystem(RenderSystem.class),
                taskManager = createSystem(TaskManager.class),
                sceneManager = createSystem(SceneManager.class),

        };
    }

    public Log getLog() {
        return log;
    }

    public GLFWLibrary getGLFWLibrary() {
        return glfwLibrary;
    }

    public Time getTimeSystem() {
        return time;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public Input getInputSystem() {
        return input;
    }

    public Graphics getGraphics() {
        return graphics;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public AssetsSystem getAssetsSystem() {
        return assetsSystem;
    }

    public RenderSystem getRenderSystem() {
        return renderSystem;
    }

    public AudioSystem getAudioSystem() {
        return audioSystem;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public SceneManager getSceneManager() {
        return sceneManager;
    }

    public void init() throws Throwable {
        Throwable error = null;
        for (RadonSystem system : systems) {
            error = initialize(system);
            if (error != null) {
                throw error;
            }
        }
    }

    private Throwable initialize(RadonSystem system) {
        try {

            double time = System.nanoTime();

            if (log.initialized()) {
                Log.info("Initializing " + system.getClass().getSimpleName() + "...");
            } else {
                Logger.getLogger(getClass().getSimpleName()).info("Initializing " + system.getClass().getSimpleName() + "...");
            }

            system.init();
            system.markInitialized();

            time = (System.nanoTime() - time) / 1e6;

            if (log.initialized()) {
                Log.info(system.getClass().getSimpleName() + " initialized in " + time + " ms");
            } else {
                Logger.getLogger(getClass().getSimpleName()).info(system.getClass().getSimpleName() + " initialized in " + time + " ms");
            }

        } catch (Throwable e) {
            Logger.getLogger(RadonSystemManager.class.getSimpleName()).log(Level.SEVERE, "Failed to initialize system " + system.getClass().getSimpleName(), e);
            return e;
        }
        return null;
    }

    public void terminate() {
        // Terminate systems in reverse order
        for (int i = systems.length - 1; i >= 0; i--) {
            terminate(systems[i]);
        }
    }

    private void terminate(RadonSystem system) {
        try {
            if (system != null && system.initialized()) {
                system.terminate();
            }
        } catch (Throwable e) {
            Logger.getLogger(RadonSystemManager.class.getSimpleName()).log(Level.SEVERE, "Failed to terminate system " + system, e);
        }
    }

    private <T extends RadonSystem> T createSystem(Class<T> clazz) {
        final T system = newInstance(clazz, this);
        TypeUtils.initSingleton(clazz, system);
        return system;
    }

}

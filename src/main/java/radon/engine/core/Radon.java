package radon.engine.core;

import radon.engine.audio.AudioSystem;
import radon.engine.events.EventManager;
import radon.engine.graphics.GraphicsAPI;
import radon.engine.graphics.rendering.APIRenderSystem;
import radon.engine.graphics.window.Window;
import radon.engine.input.Input;
import radon.engine.logging.Log;
import radon.engine.materials.MaterialManager;
import radon.engine.scenes.Scene;
import radon.engine.scenes.SceneManager;
import radon.engine.tasks.TaskManager;
import radon.engine.util.Version;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static radon.engine.core.RadonConfigConstants.*;
import static radon.engine.util.SystemInfo.*;
import static radon.engine.util.types.TypeUtils.initSingleton;

public class Radon {

    public static final Version RADON_VERSION = new Version(1, 0, 14);
    public static final String RADON_NAME = "Radon";
    public static final String GRAPHICS_THREAD_NAME = Thread.currentThread().getName();
    public static final AtomicBoolean LAUNCHED = new AtomicBoolean(false);
    private static final int UPDATES_PER_SECOND = 60;
    private static final float IDEAL_FRAME_DELAY = 1.0f / UPDATES_PER_SECOND;
    private static LaunchType LAUNCH_TYPE = LaunchType.BUILD_VERSION;
    private static boolean isRuntime = false;

    private final RadonApplication application;
    private final RadonSystemManager systems;
    private APIRenderSystem renderSystem;
    private AudioSystem audioSystem;
    private Window window;
    private float updateDelay;
    private int updatesPerSecond;
    private int framesPerSecond;

    private Radon(RadonApplication application) {
        this.application = application;

        initSingleton(RadonApplication.class, application);
        systems = new RadonSystemManager();
    }

    public static synchronized void launch(RadonApplication application) {

        if (!LAUNCHED.compareAndSet(false, true)) {
            throw new ExceptionInInitializerError("Radon has been already launched");
        }

        if (application.getClass().getName().equals("radon.editor.core.RadonEditor")) {
            LAUNCH_TYPE = LaunchType.RADON_EDITOR;
        }

        RadonConfigConstants.ensureLoaded();

        Radon radon = new Radon(requireNonNull(application));

        try {
            radon.init();
            radon.run();
        } catch (Throwable error) {
            radon.error(error);
        } finally {
            radon.terminate();
        }
    }

    public static boolean isEditor() {
        return LAUNCH_TYPE == LaunchType.RADON_EDITOR;
    }

    public static boolean startRuntime() {
        if (LAUNCH_TYPE == LaunchType.RADON_EDITOR) {

            if (isRuntime) {
                Log.warning("Already started");
            } else {
                isRuntime = true;

                return true;
            }
        } else {
            Log.error("Radon editor not launched");
        }

        return false;
    }

    public static boolean stopRuntime() {

        if (LAUNCH_TYPE == LaunchType.RADON_EDITOR) {

            if (!isRuntime) {
                Log.warning("Not started");
            } else {
                isRuntime = false;

                return true;
            }
        } else {
            Log.error("Radon editor not launched");
        }

        return false;
    }

    public static boolean isRuntime() {
        return LAUNCH_TYPE == LaunchType.RADON_EDITOR && isRuntime;
    }

    private void init() throws Throwable {
        System.out.println(RADON_NAME + " version (" + RADON_VERSION.toString() + ")");

        application.onInit();

        systems.init();

        Log.info("Radon Systems initialized successfully");
    }

    private void run() {

        Log.info("Starting Application...");

        application.start(getFirstScene());

        renderSystem = systems.getRenderSystem().getAPIRenderSystem();
        audioSystem = systems.getAudioSystem();
        window = Window.get();

        setup();

        final Time time = systems.getTimeSystem();

        float lastFrame = Time.time();
        float lastDebugReport = Time.time();
        float deltaTime;

        while (application.running()) {

            final float now = Time.time();
            time.deltaTime = deltaTime = now - lastFrame;
            lastFrame = now;

            update(deltaTime);

            render();

            ++framesPerSecond;

            ++time.frames;

            if (SHOW_DEBUG_INFO && Time.time() - lastDebugReport >= 1.0f) {
                Log.debug(buildDebugReport(framesPerSecond, updatesPerSecond, deltaTime));
                time.ups = updatesPerSecond;
                time.fps = framesPerSecond;
                updatesPerSecond = 0;
                framesPerSecond = 0;
                lastDebugReport = Time.time();
            }
        }
    }

    private Scene getFirstScene() {

        if (FIRST_SCENE_NAME == null) {
            return null;
        }

        Scene scene = SceneManager.newScene(FIRST_SCENE_NAME);

        SceneManager.setScene(scene);

        return scene;
    }

    private void setup() {

        if (WINDOW_VISIBLE) {
            Window.get().show();
        }

        update(IDEAL_FRAME_DELAY);

        render();
    }

    private void update(float deltaTime) {

        final EventManager eventManager = systems.getEventManager();
        final Input input = systems.getInputSystem();
        final SceneManager sceneManager = systems.getSceneManager();
        final AudioSystem audio = audioSystem;
        final MaterialManager materials = MaterialManager.get();
        final TaskManager taskManager = systems.getTaskManager();

        updateDelay += deltaTime;

        int updates = 0;

        boolean wasUpdated = false;

        while (updates < UPDATES_PER_SECOND && updateDelay >= IDEAL_FRAME_DELAY) {

            eventManager.processEvents();

            input.update();

            audio.update();

            sceneManager.update();

            application.onUpdate();

            wasUpdated = true;

            updateDelay -= IDEAL_FRAME_DELAY;
            ++updatesPerSecond;
            ++updates;
        }

        if (wasUpdated) {
            materials.update();
            sceneManager.endUpdate();
        }

        taskManager.executeGraphicsTasks();
    }

    private void render() {

        if (window.visible()) {

            renderSystem.begin();

            application.onRenderBegin();

            systems.getSceneManager().render();

            application.onRenderEnd();

            renderSystem.end();
        }
    }

    private void error(Throwable error) {
        application.onError(error);
    }

    private void terminate() {

        Log.info("Exiting Application...");

        application.onTerminate();

        systems.terminate();
    }

    private String buildDebugReport(int fps, int ups, float deltaTime) {

        StringBuilder builder = new StringBuilder(
                format("FPS: %d | UPS: %d | DeltaTime: %.6fs | Time: %s | Graphics API: %s",
                        fps, ups, deltaTime, Time.format(), GraphicsAPI.get()));

        if (SHOW_DEBUG_INFO_ON_WINDOW_TITLE) {
            Window.get().title(APPLICATION_NAME + " | [DEBUG INFO]: "
                    + builder.toString()
                    + " | Memory used: " + memoryUsed() / 1024 / 1024 + " MB"
                    + " | Total memory: " + totalMemory() / 1024 / 1024 + " MB");
        }

        builder.append("\n\t");

        if (MEMORY_USAGE_REPORT) {
            builder.append("[JVM MEMORY]: Used = ").append(memoryUsed() / 1024 / 1024)
                    .append(" MB | Total = ").append(totalMemory() / 1024 / 1024)
                    .append(" MB | Max = ").append(maxMemory() / 1024 / 1024).append(" MB");

            builder.append("\n\t");
        }

        if (EVENTS_DEBUG_REPORT) {
            builder.append("[EVENT-MANAGER]: ").append(systems.getEventManager().debugReport());
            builder.append("\n\t");
        }

        if (SCENES_DEBUG_REPORT) {
            builder.append("[SCENE-MANAGER]: ").append(systems.getSceneManager().debugReport());
        }

        return builder.toString();
    }

    public enum LaunchType {
        RADON_EDITOR,
        BUILD_VERSION
    }
}

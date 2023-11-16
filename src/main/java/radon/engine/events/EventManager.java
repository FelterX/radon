package radon.engine.events;

import radon.engine.core.RadonSystem;
import radon.engine.core.RadonSystemManager;
import radon.engine.util.types.Singleton;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

import static java.util.stream.IntStream.range;
import static org.lwjgl.glfw.GLFW.*;
import static radon.engine.core.RadonConfigConstants.EVENTS_DEBUG_REPORT;
import static radon.engine.util.Asserts.assertNonNull;

public final class EventManager extends RadonSystem {

    @Singleton
    private static EventManager instance;
    private Queue<Event> frontEventQueue;
    private Queue<Event> backEventQueue;
    private Map<Class<? extends Event>, List<EventCallback<?>>> eventCallbacks;
    private EventDispatcher dispatcher;
    private EventDebugReport debugReport;

    private EventManager(RadonSystemManager systemManager) {
        super(systemManager);
    }

    public static <T extends Event> void addEventCallback(Class<T> eventClass, EventCallback<T> callback) {
        instance.addEventCallbackInternal(assertNonNull(eventClass), assertNonNull(callback));
    }

    public static <T extends Event> void pushEventCallback(Class<T> eventClass, EventCallback<T> callback) {
        instance.pushEventCallbackInternal(assertNonNull(eventClass), assertNonNull(callback));
    }

    public static void removeEventCallback(Class<? extends Event> eventClass, EventCallback<?> eventCallback) {
        instance.removeEventCallbackInternal(assertNonNull(eventClass), eventCallback);
    }

    public static void triggerEventNow(Event event) {
        instance.dispatcher.dispatch(event);
    }

    public static void triggerEvent(Event event) {
        instance.frontEventQueue.add(assertNonNull(event));
    }

    public static void waitForEvents() {
        glfwWaitEvents();
    }

    public static void waitForEvents(double timeout) {
        glfwWaitEventsTimeout(timeout);
    }

    @Override
    protected void init() {
        this.frontEventQueue = new ConcurrentLinkedDeque<>();
        this.backEventQueue = new ConcurrentLinkedDeque<>();
        this.eventCallbacks = new HashMap<>();
        dispatcher = new EventDispatcher(eventCallbacks);
        debugReport = EVENTS_DEBUG_REPORT ? new EventDebugReport() : null;
    }

    @Override
    protected void terminate() {

    }

    public void processEvents() {

        glfwPollEvents();

        if (EVENTS_DEBUG_REPORT) {
            debugReport.count(frontEventQueue.size());
        }

        processEventQueue();
    }

    public CharSequence debugReport() {
        return EVENTS_DEBUG_REPORT ? instance.debugReport.report() : null;
    }

    private void processEventQueue() {

        final Queue<Event> eventQueue = frontEventQueue;
        final EventDispatcher dispatcher = this.dispatcher;

        if (eventQueue.isEmpty()) {
            return;
        }

        swapEventQueues();

        range(0, eventQueue.size()).unordered().forEach(i -> dispatcher.dispatch(eventQueue.poll()));
    }

    private void swapEventQueues() {
        final Queue<Event> tmp = this.frontEventQueue;
        this.frontEventQueue = backEventQueue;
        backEventQueue = tmp;
    }

    private void addEventCallbackInternal(Class<? extends Event> eventClass, EventCallback<? extends Event> callback) {
        List<EventCallback<?>> callbacks = eventCallbacks.computeIfAbsent(eventClass, k -> new ArrayList<>(1));
        callbacks.add(callback);
    }

    private void pushEventCallbackInternal(Class<? extends Event> eventClass, EventCallback<? extends Event> callback) {
        List<EventCallback<?>> callbacks = eventCallbacks.computeIfAbsent(eventClass, k -> new ArrayList<>(1));
        callbacks.add(0, callback);
    }

    private void removeEventCallbackInternal(Class<? extends Event> eventClass, EventCallback<?> eventCallback) {
        List<EventCallback<?>> callbacks = eventCallbacks.get(eventClass);
        if (callbacks != null) {
            callbacks.remove(eventCallback);
        }
    }

    private class EventDebugReport {

        private int eventCount;
        private int maxEventCount;

        private void count(int count) {
            eventCount += count;
        }

        public String report() {

            maxEventCount = Math.max(maxEventCount, eventCount);
            final int eventCount = this.eventCount;
            this.eventCount = 0;

            return "Event count: " + eventCount + " | Max Event count: " + maxEventCount;
        }
    }
}

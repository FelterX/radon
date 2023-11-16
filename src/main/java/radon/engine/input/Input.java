package radon.engine.input;

import org.lwjgl.glfw.GLFWJoystickCallback;
import org.lwjgl.system.MemoryStack;
import radon.engine.core.RadonSystem;
import radon.engine.core.RadonSystemManager;
import radon.engine.events.EventManager;
import radon.engine.events.input.joystick.JoystickConnectedEvent;
import radon.engine.events.input.joystick.JoystickDisconnectedEvent;
import radon.engine.events.input.keyboard.KeyPressedEvent;
import radon.engine.events.input.keyboard.KeyReleasedEvent;
import radon.engine.events.input.keyboard.KeyRepeatEvent;
import radon.engine.events.input.keyboard.KeyTypedEvent;
import radon.engine.events.input.mouse.*;
import radon.engine.graphics.window.Window;
import radon.engine.logging.Log;
import radon.engine.util.types.Singleton;

import java.nio.DoubleBuffer;
import java.util.EnumMap;

import static org.lwjgl.glfw.GLFW.*;

public final class Input extends RadonSystem {

    @Singleton
    private static Input instance;


    public static boolean isKeyReleased(Key key) {
        return instance.keyStates.isReleased(key);
    }

    public static boolean isKeyPressed(Key key) {
        return instance.keyStates.isPressed(key) || isKeyRepeat(key);
    }

    public static boolean isKeyRepeat(Key key) {
        return instance.keyStates.isRepeat(key);
    }

    public static boolean isKeyTyped(Key key) {
        return instance.keyStates.isType(key);
    }

    public static State stateOf(Key key) {
        return instance.keyStates.stateOf(key);
    }

    public static boolean isMouseButtonReleased(MouseButton button) {
        return instance.mouseButtonStates.isReleased(button);
    }

    public static boolean isMouseButtonPressed(MouseButton button) {
        return instance.mouseButtonStates.isPressed(button);
    }

    public static boolean isMouseButtonRepeat(MouseButton button) {
        return instance.mouseButtonStates.isRepeat(button);
    }

    public static boolean isMouseButtonClicked(MouseButton button) {
        return instance.mouseButtonStates.isClick(button);
    }

    public static State stateOf(MouseButton button) {
        return instance.mouseButtonStates.stateOf(button);
    }

    public static float mouseX() {
        return instance.mouseX;
    }

    public static float mouseY() {
        return instance.mouseY;
    }

    public static float scrollX() {
        return instance.mouseScrollX;
    }

    public static float scrollY() {
        return instance.mouseScrollY;
    }

    public static Gamepad gamepad(Joystick joystick) {

        if(!joystick.isPresent() || !joystick.isGamepad()) {
            return null;
        }

        return instance.gamepads.computeIfAbsent(joystick, j -> new Gamepad(joystick));
    }


    private final StateTable<Key> keyStates;
    private final StateTable<MouseButton> mouseButtonStates;
    private final EnumMap<Joystick, Gamepad> gamepads;
    private final GLFWJoystickCallback joystickCallback;
    private float mouseX, mouseY;
    private float mouseScrollX, mouseScrollY;

    private Input(RadonSystemManager systemManager) {
        super(systemManager);
        keyStates = new StateTable<>(Key.class);
        mouseButtonStates = new StateTable<>(MouseButton.class);
        gamepads = new EnumMap<>(Joystick.class);
        joystickCallback = GLFWJoystickCallback.create(this::onJoystickEvent);
    }

    @Override
    protected void init() {
        setEventCallbacks();
    }

    @Override
    protected void terminate() {
        joystickCallback.free();
    }

    public void update() {
        gamepads.values().parallelStream().unordered().forEach(Gamepad::update);
    }

    private void cacheMousePosition() {

        final Window window = Window.get();

        try(MemoryStack stack = MemoryStack.stackPush()) {

            DoubleBuffer x = stack.mallocDouble(1);
            DoubleBuffer y = stack.mallocDouble(1);

            glfwGetCursorPos(window.handle(), x, y);

            mouseX = (float) x.get(0);
            mouseY = (float) y.get(0);
        }
    }

    private void setEventCallbacks() {
        setKeyboardEventCallbacks();
        setMouseEventCallbacks();
        setJoystickCallbacks();
    }

    private void setJoystickCallbacks() {

        glfwSetJoystickCallback(joystickCallback);

        EventManager.addEventCallback(JoystickConnectedEvent.class, e -> {
            Log.info(e.joystick() + " has been connected");
            Gamepad.of(e.joystick());
        });

        EventManager.addEventCallback(JoystickConnectedEvent.class, e -> {
            Log.info(e.joystick() + " has been disconnected");
            gamepads.remove(e.joystick());
        });
    }

    private void setKeyboardEventCallbacks() {

        EventManager.pushEventCallback(KeyPressedEvent.class, e -> keyStates.set(e.key(), State.PRESS));

        EventManager.pushEventCallback(KeyRepeatEvent.class, e -> keyStates.set(e.key(), State.REPEAT));

        EventManager.pushEventCallback(KeyTypedEvent.class, e -> {

            keyStates.set(e.key(), State.TYPE);

            // Reset to released afterwards
            EventManager.triggerEvent(new KeyReleasedEvent(e.key(), e.modifiers()));
        });

        EventManager.pushEventCallback(KeyReleasedEvent.class, e -> keyStates.set(e.key(), State.RELEASE));
    }

    private void setMouseEventCallbacks() {

        EventManager.pushEventCallback(MouseMovedEvent.class, e -> cacheMousePosition());

        EventManager.pushEventCallback(MouseButtonPressedEvent.class, e -> mouseButtonStates.set(e.button(), State.PRESS));

        EventManager.pushEventCallback(MouseButtonClickedEvent.class, e -> {

            mouseButtonStates.set(e.button(), State.CLICK);

            // Reset to released afterwards
            EventManager.triggerEvent(new MouseButtonReleasedEvent(e.button(), e.modifiers()));
        });

        EventManager.pushEventCallback(MouseButtonReleasedEvent.class, e -> mouseButtonStates.set(e.button(), State.RELEASE));

        final class ClearMouseScrollEvent extends MouseEvent {}

        EventManager.pushEventCallback(ClearMouseScrollEvent.class, e -> mouseScrollX = mouseScrollY = 0.0f);

        EventManager.pushEventCallback(MouseScrollEvent.class, e -> {

            mouseScrollX = e.getXOffset();
            mouseScrollY = e.getYOffset();

            EventManager.triggerEvent(new ClearMouseScrollEvent());
        });
    }

    private void onJoystickEvent(int jid, int event) {

        Joystick joystick = Joystick.asJoystick(jid);

        EventManager.triggerEvent(event == GLFW_CONNECTED ? new JoystickConnectedEvent(joystick) : new JoystickDisconnectedEvent(joystick));
    }

}

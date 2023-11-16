package radon.engine.events.input.joystick;

import radon.engine.events.Event;
import radon.engine.input.Joystick;

public abstract class JoystickEvent extends Event {

    private final Joystick joystick;

    protected JoystickEvent(Joystick joystick) {
        this.joystick = joystick;
    }

    public Joystick joystick() {
        return joystick;
    }

    @Override
    public Class<? extends Event> type() {
        return JoystickEvent.class;
    }
}

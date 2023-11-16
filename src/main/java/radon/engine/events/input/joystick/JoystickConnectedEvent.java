package radon.engine.events.input.joystick;

import radon.engine.input.Joystick;

public class JoystickConnectedEvent extends JoystickEvent {

    public JoystickConnectedEvent(Joystick joystick) {
        super(joystick);
    }
}

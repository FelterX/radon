package radon.engine.events.input.joystick;


import radon.engine.input.Joystick;

public class JoystickDisconnectedEvent extends JoystickEvent {

    public JoystickDisconnectedEvent(Joystick joystick) {
        super(joystick);
    }
}

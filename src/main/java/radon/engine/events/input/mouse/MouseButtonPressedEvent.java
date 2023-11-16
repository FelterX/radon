package radon.engine.events.input.mouse;

import radon.engine.input.KeyModifier;
import radon.engine.input.MouseButton;

import java.util.Set;

public class MouseButtonPressedEvent extends MouseButtonEvent {

	public MouseButtonPressedEvent(MouseButton button, Set<KeyModifier> modifiers) {
		super(button, modifiers);
	}

	

}

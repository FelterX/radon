package radon.engine.events.input.mouse;

import radon.engine.input.KeyModifier;
import radon.engine.input.MouseButton;

import java.util.Set;

public class MouseButtonReleasedEvent extends MouseButtonEvent {

	public MouseButtonReleasedEvent(MouseButton button, Set<KeyModifier> modifiers) {
		super(button, modifiers);
	}



}

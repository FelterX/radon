package radon.engine.events.input.mouse;

import radon.engine.input.KeyModifier;
import radon.engine.input.MouseButton;

import java.util.Set;

public class MouseButtonClickedEvent extends MouseButtonEvent {

	public MouseButtonClickedEvent(MouseButton button, Set<KeyModifier> modifiers) {
		super(button, modifiers);
	}

}

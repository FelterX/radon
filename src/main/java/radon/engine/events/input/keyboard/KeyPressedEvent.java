package radon.engine.events.input.keyboard;

import radon.engine.input.Key;
import radon.engine.input.KeyModifier;

import java.util.Set;

public class KeyPressedEvent extends KeyEvent {

	public KeyPressedEvent(Key key, Set<KeyModifier> modifiers) {
		super(key, modifiers);
	}

}

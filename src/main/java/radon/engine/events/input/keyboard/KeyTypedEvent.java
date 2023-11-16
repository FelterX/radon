package radon.engine.events.input.keyboard;

import radon.engine.input.Key;
import radon.engine.input.KeyModifier;

import java.util.Set;

public class KeyTypedEvent extends KeyEvent {

	public KeyTypedEvent(Key key, Set<KeyModifier> modifiers) {
		super(key, modifiers);
	}

}

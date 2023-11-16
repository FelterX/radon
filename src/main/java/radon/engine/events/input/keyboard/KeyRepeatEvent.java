package radon.engine.events.input.keyboard;

import radon.engine.input.Key;
import radon.engine.input.KeyModifier;

import java.util.Set;

public class KeyRepeatEvent extends KeyEvent {
	
	private final int repeatCount;

	public KeyRepeatEvent(Key key, Set<KeyModifier> modifiers, int repeatCount) {
		super(key, modifiers);
		this.repeatCount = repeatCount;
	}

	public int repeatCount() {
		return repeatCount;
	}
	
}

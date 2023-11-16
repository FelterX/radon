package radon.engine.events.window;

import radon.engine.events.Event;

public abstract class WindowEvent extends Event {

	@Override
	public Class<? extends Event> type() {
		return WindowEvent.class;
	}
}





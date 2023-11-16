package radon.engine.events.input.mouse;

import radon.engine.events.Event;

public abstract class MouseEvent extends Event {

    @Override
    public Class<? extends Event> type() {
        return MouseEvent.class;
    }
}

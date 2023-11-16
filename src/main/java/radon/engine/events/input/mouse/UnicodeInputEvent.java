package radon.engine.events.input.mouse;

import radon.engine.events.Event;

public class UnicodeInputEvent extends Event {

    private final int codePoint;

    public UnicodeInputEvent(int codePoint) {
        this.codePoint = codePoint;
    }

    public int codePoint() {
        return codePoint;
    }

    @Override
    public Class<? extends Event> type() {
        return UnicodeInputEvent.class;
    }
}

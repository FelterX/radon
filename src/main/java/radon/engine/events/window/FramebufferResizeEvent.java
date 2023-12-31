package radon.engine.events.window;

import radon.engine.events.Event;

public class FramebufferResizeEvent extends Event {

    private final int width;
    private final int height;

    public FramebufferResizeEvent(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    @Override
    public Class<? extends Event> type() {
        return FramebufferResizeEvent.class;
    }
}

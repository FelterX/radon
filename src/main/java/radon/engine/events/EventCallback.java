package radon.engine.events;

public interface EventCallback<T extends Event> {

    void onEvent(T event);

}

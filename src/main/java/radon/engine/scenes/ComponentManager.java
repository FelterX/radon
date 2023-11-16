package radon.engine.scenes;

public abstract class ComponentManager<T extends Component> {


    private final Scene scene;

    protected ComponentManager(Scene scene) {
        this.scene = scene;
    }

    public final Scene scene() {
        return scene;
    }

    protected abstract void add(T component);

    protected abstract void enable(T component);

    protected abstract void disable(T component);

    protected abstract void remove(T component);

    protected abstract void removeAll();

    protected abstract int size();
}

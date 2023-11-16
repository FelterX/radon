package radon.engine.scenes;

import radon.engine.logging.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static radon.engine.util.Asserts.assertNonNull;
import static radon.engine.util.types.TypeUtils.getOrElse;
import static radon.engine.util.types.TypeUtils.newInstance;

public final class Entity extends SceneObject implements Iterable<Component> {

    public static final int INVALID_INDEX = -1;
    public static final String UNNAMED = "__UNNAMED";
    public static final String UNTAGGED = "__UNTAGGED";

    public static Entity newEntity() {
        if(noAvailableScene()) {
            return null;
        }
        return SceneManager.scene().newEntity();
    }

    public static Entity newEntity(String name) {
        if(noAvailableScene()) {
            return null;
        }
        return SceneManager.scene().newEntity(name);
    }

    public static Entity newEntity(String name, String tag) {
        if(noAvailableScene()) {
            return null;
        }
        return SceneManager.scene().newEntity(name, tag);
    }

    private static boolean noAvailableScene() {
        if(SceneManager.scene() == null) {
            Log.fatal("There is no scene available to create the Entity");
            return true;
        }
        return false;
    }

    private String name;
    private String tag;
    private Scene scene;
    private Map<Class<? extends Component>, Component> components;
    private int index;
    private boolean enabled;

    Entity(String name, String tag, Scene scene, int index) {
        components = new HashMap<>();
        init(name, tag, scene, index);
    }

    public synchronized <T extends Component> T add(Class<T> componentClass) {
        assertNonNull(componentClass);

        if(has(componentClass)) {
            return getComponent(componentClass);
        }

        T component = newInstance(componentClass);
        component.init();
        component.entity = this;

        components.put(componentClass, component);

        if(!destroyed()) {
            doLater(() -> scene.add(component));
        }

        return component;
    }

    public <T extends Component> T get(Class<T> componentClass) {
        assertNonNull(componentClass);

        if(has(componentClass)) {
            return getComponent(componentClass);
        }

        return add(componentClass);
    }

    private <T extends Component> T getComponent(Class<T> componentClass) {
        return componentClass.cast(components.get(componentClass));
    }

    public <T extends Component> T requires(Class<T> componentClass) {
        assertNonNull(componentClass);

        if(has(componentClass)) {
            return get(componentClass);
        }

        throw new NoSuchElementException("Component of class " + componentClass.getSimpleName() + " required but not present");
    }

    public void destroy(Class<? extends Component> componentClass) {
        destroy(get(componentClass));
    }

    public void destroy(Component component) {

        if(component == null || component.destroyed() || component.entity() != this) {
            return;
        }

        scene.destroy(component);
        doLater(() -> components.remove(component.getClass()));
    }

    public void destroyNow(Component component) {

        if(component == null || component.destroyed() || component.entity() != this) {
            return;
        }

        scene.destroyNow(component);
        components.remove(component.getClass());
    }

    public boolean has(Class<? extends Component> componentClass) {
        return components.containsKey(componentClass);
    }

    public boolean has(Component component) {
        return components.containsValue(component);
    }

    public int componentCount() {
        return components.size();
    }

    public Stream<Component> components() {
        return components.values().stream();
    }

    public Stream<Component> components(Class<? extends Component> type) {
        return this.components.values().stream().filter(c -> c.type().equals(type));
    }

    public String name() {
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public String tag() {
        return tag;
    }

    @Override
    public Scene scene() {
        return scene;
    }

    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public Entity enable() {
        if(!enabled) {
            components.values().forEach(Component::enable);
            enabled = true;
        }
        return this;
    }

    @Override
    public Entity disable() {
        if(enabled) {
            components.values().forEach(Component::disable);
            enabled = false;
        }
        return this;
    }

    @Override
    public void destroy() {
        scene.destroy(this);
    }

    @Override
    public void destroyNow() {
        scene.destroyNow(this);
    }

    @Override
    public Iterator<Component> iterator() {
        return components.values().iterator();
    }

    @Override
    protected void onDestroy() {
        components.values().forEach(scene::destroyComponent);
        index = INVALID_INDEX;
        name = null;
        tag = null;
        scene = null;
        components.clear();
        components = null;
    }

    int index() {
        return index;
    }

    void init(String name, String tag, Scene scene, int index) {
        this.name = getOrElse(name, UNNAMED);
        this.tag = getOrElse(tag, UNTAGGED);
        this.scene = assertNonNull(scene);
        this.index = index;
        enabled = true;
    }

}

package radon.engine.scenes;

public abstract class Component<SELF extends Component> extends SceneObject {

    Entity entity;
    ComponentManager<SELF> manager;
    private boolean enabled;


    @Override
    protected void init() {
        super.init();
        enabled = true;
    }

    public Entity entity() {
        assertNotDeleted();
        return entity;
    }

    public String name() {
        assertNotDeleted();
        return entity.name();
    }

    public String tag() {
        assertNotDeleted();
        return entity.tag();
    }

    public final boolean active() {
        assertNotDeleted();
        return manager != null;
    }

    public final <T extends Component> T get(Class<T> componentClass) {
        assertNotDeleted();
        return entity.get(componentClass);
    }

    public final <T extends Component> T requires(Class<T> componentClass) {
        assertNotDeleted();
        return entity.requires(componentClass);
    }

    protected ComponentManager<SELF> manager() {
        assertNotDeleted();
        return manager;
    }

    @Override
    public Scene scene() {
        assertNotDeleted();
        return entity().scene();
    }

    @Override
    public boolean enabled() {
        assertNotDeleted();
        return enabled;
    }

    @Override
    public final SELF enable() {
        assertNotDeleted();
        if(!enabled() && active()) {
            doLater(() -> {
                manager.enable(self());
                onEnable();
            });
        }
        return self();
    }

    @Override
    public final SELF disable() {
        assertNotDeleted();
        if(enabled() && active()) {
            doLater(() -> {
                manager.disable(self());
                onDisable();
            });
        }
        return self();
    }

    public abstract Class<? extends Component> type();

    @Override
    public boolean destroyed() {
        return super.destroyed() || entity.destroyed();
    }

    @Override
    public final void destroy() {
        assertNotDeleted();
        if(active()) {
            entity.destroy(this);
        }
    }

    @Override
    public void destroyNow() {
        assertNotDeleted();
        if(active()) {
            entity.destroyNow(this);
        }
    }

    protected abstract void onEnable();
    protected abstract void onDisable();
    protected abstract SELF self();
    public void executeGui(String name){}
}

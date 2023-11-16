package radon.engine.scenes.components.behaviours;

import radon.engine.scenes.Component;

import static radon.engine.util.Asserts.assertTrue;

public abstract class AbstractBehaviour extends Component<AbstractBehaviour> {

    private boolean started;

    protected AbstractBehaviour() {

    }

    @Override
    protected void init() {
        super.init();
        assertTrue(this instanceof IUpdateBehaviour || this instanceof ILateBehaviour);
        started = false;
        onInit();
    }

    public boolean started() {
        assertNotDeleted();
        return started;
    }

    void start() {
        onStart();
        started = true;
    }

    protected void onInit() {

    }

    protected void onStart() {

    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    @Override
    protected void onDestroy() {

    }

    @Override
    public final Class<? extends Component> type() {
        return AbstractBehaviour.class;
    }

    @Override
    protected final AbstractBehaviour self() {
        return this;
    }
}

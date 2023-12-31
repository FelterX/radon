package radon.engine.scenes.components.meshes;

import org.joml.Matrix4fc;
import radon.engine.logging.Log;
import radon.engine.meshes.views.MeshView;
import radon.engine.scenes.Component;
import radon.engine.scenes.components.math.Transform;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static radon.engine.util.Asserts.assertNonNull;

public abstract class MeshInstance<T extends MeshView> extends Component<MeshInstance> implements Iterable<T> {

    protected List<T> meshViews;

    protected MeshInstance() {

    }

    @Override
    protected void init() {
        super.init();
        meshViews = null;
    }

    public abstract Class<T> meshViewType();

    public T meshView() {
        return meshView(0);
    }

    public T meshView(int index) {
        return meshViews.get(index);
    }

    public int numMeshViews() {
        return meshViews.size();
    }

    public MeshInstance meshView(T meshView) {
        return meshViews(meshView);
    }

    public MeshInstance meshViews(T meshView) {
        if(meshViews != null) {
            Log.error("Cannot modify Mesh Instance component once you have set its Mesh Views");
        } else {
            assertNonNull(meshView);
            this.meshViews = Collections.singletonList(meshView);
            doLater(() -> manager().enable(this));
        }
        return this;
    }

    public Transform transform() {
        return requires(Transform.class);
    }

    public Matrix4fc modelMatrix() {
        return requires(Transform.class).modelMatrix();
    }

    @Override
    public final Class<? extends Component> type() {
        return MeshInstance.class;
    }

    @Override
    public boolean enabled() {
        return super.enabled() && meshViews != null;
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    @Override
    protected MeshInstance self() {
        return this;
    }

    @Override
    protected void onDestroy() {

    }

    public Stream<T> meshViews() {
        return meshViews.stream();
    }

    @Override
    public Iterator<T> iterator() {
        return meshViews.iterator();
    }

    @Override
    protected MeshInstanceManager manager() {
        return (MeshInstanceManager) super.manager();
    }
}

package radon.engine.scenes.components.math;

import radon.engine.scenes.Scene;
import radon.engine.scenes.components.AbstractComponentManager;

import java.util.stream.Stream;

public final class TransformManager extends AbstractComponentManager<Transform> {

    private TransformManager(Scene scene) {
        super(scene);
    }

    public void update() {
        modifiedTransforms().forEach(Transform::update);
    }

    private Stream<Transform> modifiedTransforms() {
        return components.enabled().parallelStream().unordered().filter(Transform::modified);
    }
}

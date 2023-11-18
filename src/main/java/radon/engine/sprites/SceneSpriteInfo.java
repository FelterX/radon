package radon.engine.sprites;

import radon.engine.scenes.components.sprites.SpriteInstance;

import java.util.List;

public interface SceneSpriteInfo {
    List<SpriteInstance> allInstances();
    List<SpriteInstance> newInstances();
    List<SpriteInstance> removeInstances();

    void onAdded();
    void onRemoved();
}

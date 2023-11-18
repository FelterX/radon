package radon.engine.scenes.components.sprites;

import radon.engine.logging.Log;
import radon.engine.scenes.Component;

import radon.engine.sprites.Sprite;

import static radon.engine.util.Asserts.assertNonNull;

public class SpriteInstance extends Component {

    protected Sprite sprite;
    protected int layerOrder;

    protected SpriteInstance() {

    }

    @Override
    protected void init() {
        super.init();
        sprite = null;
        layerOrder = 0;
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
    protected SpriteInstanceManager manager() {
        return (SpriteInstanceManager) super.manager();
    }

    @Override
    protected Component self() {
        return this;
    }

    @Override
    public Class<? extends Component> type() {
        return SpriteInstance.class;
    }

    public SpriteInstance sprite(Sprite sprite, int layer) {
        if (this.sprite != null) {
            Log.error("Cannot modify Sprite Instance component once you have set its Sprite");
        } else {

            this.sprite = assertNonNull(sprite);
            this.layerOrder = layer;
            doLater(() -> manager().enable(this));
        }
        return this;
    }

    public Sprite sprite(){return this.sprite;}

    public int layerOrder() {
        return layerOrder;
    }
}

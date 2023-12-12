package radon.engine.scenes.components.sprites;

import org.joml.Vector2f;
import radon.engine.logging.Log;
import radon.engine.scenes.Component;

import radon.engine.sprites.Sprite;

import static radon.engine.util.Asserts.assertNonNull;

public class SpriteInstance extends Component {

    protected Sprite sprite;
    protected int layerOrder;
    protected Vector2f anchor;

    private boolean modified;

    protected SpriteInstance() {

    }

    @Override
    protected void init() {
        super.init();
        sprite = null;
        layerOrder = 0;
        anchor = new Vector2f(0.5f, 0.5f);
    }

    protected void update() {
        modified = false;
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

    public SpriteInstance sprite(Sprite sprite) {
        this.sprite = sprite;
        modify();
        return this;
    }

    public SpriteInstance layerOrder(int layerOrder) {
        this.layerOrder = layerOrder;
        modify();
        return this;
    }

    public SpriteInstance anchor(float x, float y) {
        return anchor(new Vector2f(x, y));
    }

    public SpriteInstance anchor(Vector2f anchor) {
        this.anchor.set(anchor);
        modify();
        return this;
    }


    public Vector2f anchor() {
        return anchor;
    }

    public Sprite sprite() {
        return this.sprite;
    }

    public int layerOrder() {
        return layerOrder;
    }

    public boolean modified() {
        assertNotDeleted();
        return modified;
    }

    public void modify() {
        modified = true;
    }
}

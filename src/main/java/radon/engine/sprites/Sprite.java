package radon.engine.sprites;

import org.joml.Vector2f;
import radon.engine.graphics.opengl.textures.GLTexture;
import radon.engine.graphics.textures.Texture;
import radon.engine.util.geometry.Rect;

import static java.util.Objects.requireNonNull;

public class Sprite {

    protected GLTexture texture;
    protected Rect bounds;

    public Sprite(GLTexture texture) {
        this(texture, new Rect(0, texture.width(), 0, texture.height()));
    }

    public Sprite(GLTexture texture, Rect bounds) {
        this.texture = texture;
        this.bounds = requireNonNull(bounds);
    }

    public GLTexture texture() {
        return texture;
    }

    public Rect bounds() {
        return bounds;
    }

    public Vector2f[] textureCoords() {
        return new Vector2f[]{
                new Vector2f(bounds.right() / (float) texture.width(), bounds.top() / (float) texture.height()),
                new Vector2f(bounds.right() / (float) texture.width(), bounds.bottom() / (float) texture.height()),
                new Vector2f(bounds.left() / (float) texture.width(), bounds.bottom() / (float) texture.height()),
                new Vector2f(bounds.left() / (float) texture.width(), bounds.top() / (float) texture.height())
        };
    }
}

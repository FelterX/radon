package radon.engine.graphics.opengl.rendering.renderers;

import radon.engine.graphics.opengl.GLContext;

public abstract class GL2DRenderer extends GLRenderer implements Comparable<GL2DRenderer> {

    private final int layer;

    public GL2DRenderer(GLContext context, int layer) {
        super(context);
        this.layer = layer;
    }

    @Override
    public int compareTo(GL2DRenderer o) {
        return Integer.compare(this.layer, o.layer);
    }

    public abstract void render();
}

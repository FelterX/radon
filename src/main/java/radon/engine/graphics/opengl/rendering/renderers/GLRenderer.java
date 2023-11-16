package radon.engine.graphics.opengl.rendering.renderers;

import radon.engine.graphics.opengl.GLContext;
import radon.engine.graphics.rendering.Renderer;

public abstract class GLRenderer implements Renderer {

    private final GLContext context;

    public GLRenderer(GLContext context) {
        this.context = context;
    }

    public GLContext context() {
        return context;
    }
}

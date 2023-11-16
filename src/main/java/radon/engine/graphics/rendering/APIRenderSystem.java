package radon.engine.graphics.rendering;

import radon.engine.scenes.Scene;

public interface APIRenderSystem {

    void init();

    void terminate();

    void begin();

    void prepare(Scene scene);

    void render(Scene scene);

    void end();
}

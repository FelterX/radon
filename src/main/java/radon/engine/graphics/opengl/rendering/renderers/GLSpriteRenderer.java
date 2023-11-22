package radon.engine.graphics.opengl.rendering.renderers;

import org.lwjgl.opengl.GLUtil;
import radon.engine.core.RadonFiles;
import radon.engine.graphics.Graphics;
import radon.engine.graphics.opengl.GLContext;
import radon.engine.graphics.opengl.shaders.GLShader;
import radon.engine.graphics.opengl.shaders.GLShaderProgram;
import radon.engine.graphics.opengl.textures.GLTexture;
import radon.engine.graphics.textures.Texture;
import radon.engine.logging.Log;
import radon.engine.scenes.Camera;
import radon.engine.scenes.Scene;
import radon.engine.scenes.components.sprites.SpriteInstance;
import radon.engine.sprites.SceneSpriteInfo;
import radon.engine.sprites.Sprite;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static radon.engine.graphics.ShaderStage.FRAGMENT_STAGE;
import static radon.engine.graphics.ShaderStage.VERTEX_STAGE;
import static radon.engine.graphics.opengl.rendering.renderers.GLSpriteBatch.TEXTURES_SLOTS;

public class GLSpriteRenderer extends GLRenderer {

    public static final Path SPRITE_VERTEX_SHADER_PATH = RadonFiles.getPath("shaders/sprite/sprite.vert");
    public static final Path SPRITE_FRAGMENT_SHADER_PATH = RadonFiles.getPath("shaders/sprite/sprite.frag");
    private static final int MAX_BATCH_SIZE = 256;

    private final List<GLSpriteBatch> batches;
    private GLShaderProgram shader;

    public GLSpriteRenderer(GLContext context) {
        super(context);
        batches = new ArrayList<>();
    }

    @Override
    public void init() {
        this.shader = createShader();
    }

    public void add(SpriteInstance instance) {
        boolean added = false;

        Sprite sprite = instance.sprite();
        for (GLSpriteBatch batch : batches) {
            if (batch.hasRoom() && batch.layerOrder() == instance.layerOrder()) {
                GLTexture tex = sprite.texture();
                if (tex == null || (batch.hasTexture(tex) || batch.hasTextureRoom())) {
                    batch.addSprite(instance);
                    added = true;
                    break;
                }
            }
        }

        if (!added) {
            GLSpriteBatch newBatch = new GLSpriteBatch(this, MAX_BATCH_SIZE, instance.layerOrder());
            newBatch.init();
            batches.add(newBatch);
            newBatch.addSprite(instance);
            Collections.sort(batches);
        }
    }

    public void remove(SpriteInstance instance) {
        for (GLSpriteBatch batch : batches) {
            if (batch.removeSprite(instance)) {
                return;
            }
        }
    }

    public void render(Scene scene) {
        SceneSpriteInfo spriteInfo = scene.spriteInfo();
        boolean added = false;

        for (SpriteInstance instance : spriteInfo.newInstances()) {
            add(instance);
            added = true;
        }
        if (added) {
            spriteInfo.onAdded();
        }

        boolean removed = false;
        for (SpriteInstance instance : spriteInfo.removeInstances()) {
            remove(instance);
            removed = true;
        }
        if (removed) {
            spriteInfo.onRemoved();
        }


        Camera camera = scene.camera();
        shader.bind();
        shader.uniformMatrix4f("uProjection", false, camera.projectionMatrix());
        shader.uniformMatrix4f("uView", false, camera.viewMatrix());
        shader.uniformArrayInt("uTextures", TEXTURES_SLOTS);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        for (GLSpriteBatch batch : batches) {
            batch.render();
        }

        glDisable(GL_BLEND);

        shader.unbind();
    }

    @Override
    public void terminate() {
        this.shader.deleteShaders();
    }

    private GLShaderProgram createShader() {
        return new GLShaderProgram(context(), "OpenGL Sprite shader")
                .attach(new GLShader(context(), VERTEX_STAGE).source(SPRITE_VERTEX_SHADER_PATH).compile())
                .attach(new GLShader(context(), FRAGMENT_STAGE).source(SPRITE_FRAGMENT_SHADER_PATH).compile())
                .link();
    }


}

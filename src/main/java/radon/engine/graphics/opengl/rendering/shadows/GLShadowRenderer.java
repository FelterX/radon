package radon.engine.graphics.opengl.rendering.shadows;

import org.lwjgl.system.MemoryStack;
import radon.engine.graphics.opengl.GLContext;
import radon.engine.graphics.opengl.buffers.GLBuffer;
import radon.engine.graphics.opengl.rendering.renderers.GLMeshRenderer;
import radon.engine.graphics.opengl.rendering.renderers.GLRenderer;
import radon.engine.graphics.opengl.textures.GLTexture2D;
import radon.engine.graphics.rendering.shadows.ShadowCascade;
import radon.engine.scenes.Scene;
import radon.engine.scenes.environment.SceneLighting;

import java.nio.ByteBuffer;

import static radon.engine.graphics.textures.Texture.makeResident;
import static radon.engine.util.types.DataType.MATRIX4_SIZEOF;


public class GLShadowRenderer extends GLRenderer implements GLShadowsInfo {

    private GLDirectionalShadowRenderer directionalShadowRenderer;
    private GLBuffer shadowsBuffer;

    public GLShadowRenderer(GLContext context) {
        super(context);
    }

    @Override
    public void init() {
        directionalShadowRenderer = new GLDirectionalShadowRenderer(context());
        shadowsBuffer = new GLBuffer(context()).name("SHADOWS BUFFER");
        shadowsBuffer.allocate(SHADOWS_BUFFER_SIZE);
        shadowsBuffer.mapMemory();
    }

    public void render(Scene scene, GLMeshRenderer meshRenderer) {

        SceneLighting lighting = scene.environment().lighting();

        if (lighting.directionalLight() == null) {
            return;
        }

        directionalShadowRenderer.bakeDirectionalShadows(scene, meshRenderer);

        updateShadowsBuffer(scene);
    }

    @Override
    public void terminate() {
        directionalShadowRenderer.terminate();
        shadowsBuffer.release();
    }

    @Override
    public GLBuffer buffer() {
        return shadowsBuffer;
    }

    @Override
    public GLTexture2D[] dirShadowMaps() {
        return directionalShadowRenderer.dirShadowMaps();
    }

    private void updateShadowsBuffer(Scene scene) {

        try (MemoryStack stack = MemoryStack.stackPush()) {

            ByteBuffer buffer = stack.calloc(SHADOWS_BUFFER_SIZE);

            final boolean shadowsEnabled = scene.renderInfo().shadowsEnabled();

            if (shadowsEnabled) {
                for (int i = 0; i < MAX_SHADOW_CASCADES_COUNT; i++) {
                    putShadowCascadeInfo(i, buffer);
                }
            }

            buffer.putInt(SHADOWS_ENABLED_OFFSET, scene.renderInfo().shadowsEnabled() ? 1 : 0);

            shadowsBuffer.copy(0, buffer.rewind());
        }
    }

    private void putShadowCascadeInfo(int index, ByteBuffer buffer) {

        final ShadowCascade shadowCascade = directionalShadowRenderer.shadowCascades()[index];

        shadowCascade.lightProjectionViewMatrix().get(buffer).position(buffer.position() + MATRIX4_SIZEOF);
        buffer.putLong(makeResident(directionalShadowRenderer.dirShadowMaps()[index]))
                .putFloat(shadowCascade.farPlane())
                .putFloat(0.0f); // Padding
    }
}

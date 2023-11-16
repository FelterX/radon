package radon.engine.graphics.opengl.skyboxpbr;

import org.lwjgl.system.MemoryStack;
import radon.engine.graphics.opengl.GLContext;
import radon.engine.graphics.opengl.buffers.GLBuffer;
import radon.engine.resource.Resource;
import radon.engine.scenes.environment.skybox.Skybox;
import radon.engine.scenes.environment.skybox.SkyboxTexture;
import radon.engine.util.types.StaticByteSize;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL31C.GL_UNIFORM_BUFFER;
import static radon.engine.graphics.textures.Texture.makeResident;
import static radon.engine.util.types.DataType.INT32_SIZEOF;

/*
 * struct Skybox {
 *
 *     layout(bindless_sampler) samplerCube irradianceMap;
 *     layout(bindless_sampler) samplerCube prefilterMap;
 *     layout(bindless_sampler) sampler2D brdfMap;
 *
 *     float maxPrefilterLOD;
 *     float prefilterLODBias;
 * };
 * */
@StaticByteSize(sizeof = GLSkyboxStruct.SIZEOF)
public final class GLSkyboxStruct implements Resource {

    public static final int SIZEOF = 32;

    private static final int SKYBOX_PRESENT_OFFSET = SIZEOF;

    private static final int SKYBOX_UNIFORM_BUFFER_SIZE = SIZEOF + INT32_SIZEOF;


    private GLBuffer uniformBuffer;

    public GLSkyboxStruct(GLContext context) {
        this.uniformBuffer = new GLBuffer(context).name("Skybox Struct Uniform Buffer");
        uniformBuffer.allocate(SKYBOX_UNIFORM_BUFFER_SIZE);
        uniformBuffer.mapMemory();
    }

    public GLSkyboxStruct update(Skybox skybox) {

        try (MemoryStack stack = MemoryStack.stackPush()) {

            ByteBuffer buffer = stack.calloc(SKYBOX_UNIFORM_BUFFER_SIZE);

            final boolean skyboxPresent = skybox != null;

            if (skyboxPresent) {

                final SkyboxTexture skyboxTexture = skybox.texture1();

                buffer.putLong(makeResident(skyboxTexture.irradianceMap()))
                        .putLong(makeResident(skyboxTexture.prefilterMap()))
                        .putLong(makeResident(skybox.brdfTexture()))
                        .putFloat(skybox.maxPrefilterLOD())
                        .putFloat(skybox.prefilterLODBias());
            }

            buffer.putInt(SKYBOX_PRESENT_OFFSET, skyboxPresent ? 1 : 0);

            uniformBuffer.copy(0, buffer.rewind());
        }

        return this;
    }

    public void bind(int binding) {
        uniformBuffer.bind(GL_UNIFORM_BUFFER, binding);
    }

    @Override
    public void release() {
        uniformBuffer.release();
    }
}

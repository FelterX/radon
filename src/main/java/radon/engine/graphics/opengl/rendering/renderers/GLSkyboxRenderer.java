package radon.engine.graphics.opengl.rendering.renderers;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import radon.engine.core.RadonFiles;
import radon.engine.graphics.opengl.GLContext;
import radon.engine.graphics.opengl.buffers.GLBuffer;
import radon.engine.graphics.opengl.shaders.GLShader;
import radon.engine.graphics.opengl.shaders.GLShaderProgram;
import radon.engine.graphics.opengl.textures.GLCubemap;
import radon.engine.graphics.opengl.vertex.GLVertexArray;
import radon.engine.meshes.Mesh;
import radon.engine.meshes.StaticMesh;
import radon.engine.scenes.Scene;
import radon.engine.scenes.environment.SceneEnvironment;
import radon.engine.scenes.environment.skybox.Skybox;

import java.nio.ByteBuffer;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL31C.GL_UNIFORM_BUFFER;
import static org.lwjgl.opengl.GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS;
import static org.lwjgl.system.MemoryStack.stackPush;
import static radon.engine.graphics.ShaderStage.FRAGMENT_STAGE;
import static radon.engine.graphics.ShaderStage.VERTEX_STAGE;
import static radon.engine.meshes.vertices.VertexLayouts.VERTEX_LAYOUT_3D;
import static radon.engine.util.handles.IntHandle.NULL;
import static radon.engine.util.types.DataType.MATRIX4_SIZEOF;

public class GLSkyboxRenderer extends GLRenderer {

    public static final Path SKYBOX_VERTEX_SHADER_PATH = RadonFiles.getPath("shaders/skybox/skybox.vert");
    public static final Path SKYBOX_FRAGMENT_SHADER_PATH = RadonFiles.getPath("shaders/skybox/skybox.frag");
    private static final int MATRICES_BUFFER_SIZE = MATRIX4_SIZEOF * 2;
    private static final int PROJECTION_MATRIX_OFFSET = 0;
    private static final int VIEW_MATRIX_OFFSET = MATRIX4_SIZEOF;
    private static final int SKYBOX_INDEX_COUNT = 36;


    private GLShaderProgram shader;

    private GLVertexArray vertexArray;
    private GLBuffer vertexBuffer;
    private GLBuffer indexBuffer;

    private GLBuffer matricesUniformBuffer;

    private Matrix4f viewMatrix;

    public GLSkyboxRenderer(GLContext context) {
        super(context);
    }

    @Override
    public void init() {

        shader = createShader();

        Mesh cubeMesh = StaticMesh.cube();

        vertexArray = new GLVertexArray(context());

        vertexBuffer = new GLBuffer(context()).name("SKYBOX VERTEX BUFFER");
        vertexBuffer.data(cubeMesh.vertexData());

        indexBuffer = new GLBuffer(context()).name("SKYBOX INDEX BUFFER");
        indexBuffer.data(cubeMesh.indexData());

        vertexArray.addVertexBuffer(0, VERTEX_LAYOUT_3D.attributeList(0), vertexBuffer);

        vertexArray.setIndexBuffer(indexBuffer);

        matricesUniformBuffer = new GLBuffer(context()).name("SKYBOX MATRICES BUFFER");
        matricesUniformBuffer.allocate(MATRICES_BUFFER_SIZE);
        matricesUniformBuffer.mapMemory();

        viewMatrix = new Matrix4f();
    }

    @Override
    public void terminate() {
        vertexArray.release();
        vertexBuffer.release();
        indexBuffer.release();
    }

    public GLShaderProgram shader() {
        return shader;
    }

    public void render(Scene scene) {

        final SceneEnvironment environment = scene.environment();
        final Skybox skybox = environment.skybox();

        if (skybox == null) {
            return;
        }

        final GLCubemap skyboxTexture1 = skybox.texture1().environmentMap();
        final GLCubemap skyboxTexture2 = skybox.texture2().environmentMap();
        final float textureBlendFactor = skybox.textureBlendFactor();

        updateMatrices(scene);

        glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glDisable(GL_CULL_FACE);

        shader.bind();

        matricesUniformBuffer.bind(GL_UNIFORM_BUFFER, 0);

        shader.uniformColorRGBA("u_FogColor", environment.fog().color());

        if (skyboxTexture1 != null) {
            shader.uniformSampler("u_SkyboxTexture1", skyboxTexture1, 0);
        }

        if (skyboxTexture2 != null) {
            shader.uniformSampler("u_SkyboxTexture2", skyboxTexture2, 1);
        }

        shader.uniformFloat("u_TextureBlendFactor", textureBlendFactor);

        shader.uniformBool("u_EnableHDR", skybox.enableHDR());

        vertexArray.bind();

        glDrawElements(GL_TRIANGLES, SKYBOX_INDEX_COUNT, GL_UNSIGNED_INT, NULL);

        shader.unbind();
    }

    private void updateMatrices(Scene scene) {

        try (MemoryStack stack = stackPush()) {

            ByteBuffer buffer = stack.calloc(MATRICES_BUFFER_SIZE);

            viewMatrix.set(scene.camera().viewMatrix()).rotateY(scene.environment().skybox().rotation());

            scene.camera().projectionMatrix().get(PROJECTION_MATRIX_OFFSET, buffer);
            viewMatrix.get(VIEW_MATRIX_OFFSET, buffer);

            matricesUniformBuffer.copy(0, buffer);
        }
    }

    private GLShaderProgram createShader() {
        return new GLShaderProgram(context(), "OpenGL Skybox shader")
                .attach(new GLShader(context(), VERTEX_STAGE).source(SKYBOX_VERTEX_SHADER_PATH).compile())
                .attach(new GLShader(context(), FRAGMENT_STAGE).source(SKYBOX_FRAGMENT_SHADER_PATH).compile())
                .link();
    }

}

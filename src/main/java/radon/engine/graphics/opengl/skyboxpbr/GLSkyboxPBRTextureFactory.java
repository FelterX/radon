package radon.engine.graphics.opengl.skyboxpbr;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import radon.engine.core.RadonFiles;
import radon.engine.graphics.GraphicsFactory;
import radon.engine.graphics.opengl.GLContext;
import radon.engine.graphics.opengl.buffers.GLBuffer;
import radon.engine.graphics.opengl.shaders.GLShader;
import radon.engine.graphics.opengl.shaders.GLShaderProgram;
import radon.engine.graphics.opengl.swapchain.GLFramebuffer;
import radon.engine.graphics.opengl.textures.GLCubemap;
import radon.engine.graphics.opengl.textures.GLTexture;
import radon.engine.graphics.opengl.textures.GLTexture2D;
import radon.engine.graphics.opengl.vertex.GLVertexArray;
import radon.engine.graphics.textures.Cubemap;
import radon.engine.graphics.textures.Texture2D;
import radon.engine.images.PixelFormat;
import radon.engine.meshes.Mesh;
import radon.engine.meshes.MeshManager;
import radon.engine.meshes.StaticMesh;
import radon.engine.resource.ManagedResource;
import radon.engine.resource.Resource;
import radon.engine.scenes.environment.skybox.SkyboxHelper;
import radon.engine.scenes.environment.skybox.pbr.SkyboxPBRTextureFactory;

import java.nio.file.Path;

import static java.lang.Math.pow;
import static java.util.Objects.requireNonNull;
import static org.lwjgl.opengl.GL40.*;
import static radon.engine.graphics.ShaderStage.FRAGMENT_STAGE;
import static radon.engine.graphics.ShaderStage.VERTEX_STAGE;
import static radon.engine.meshes.PrimitiveMeshNames.QUAD_MESH_NAME;
import static radon.engine.meshes.vertices.VertexLayouts.VERTEX_LAYOUT_3D;
import static radon.engine.util.Maths.radians;
import static radon.engine.util.handles.IntHandle.NULL;

public class GLSkyboxPBRTextureFactory extends ManagedResource implements SkyboxPBRTextureFactory {

    public static final Path BRDF_VERTEX_SHADER_PATH = RadonFiles.getPath("shaders/skybox/brdf.vert");
    public static final Path BRDF_FRAGMENT_SHADER_PATH = RadonFiles.getPath("shaders/skybox/brdf.frag");
    public static final String ENVIRONMENT_MAP_UNIFORM_NAME = "u_EnvironmentMap";
    public static final String EQUIRECTANGULAR_MAP_UNIFORM_NAME = "u_EquirectangularMap";
    public static final String PROJECTION_VIEW_MATRIX_UNIFORM_NAME = "u_ProjectionViewMatrix";
    private static final Path IRRADIANCE_VERTEX_SHADER_PATH = RadonFiles.getPath("shaders/skybox/irradiance_map.vert");
    private static final Path IRRADIANCE_FRAGMENT_SHADER_PATH = RadonFiles.getPath("shaders/skybox/irradiance_map.frag");
    private static final Path PREFILTER_VERTEX_SHADER_PATH = RadonFiles.getPath("shaders/skybox/prefilter_map.vert");
    private static final Path PREFILTER_FRAGMENT_SHADER_PATH = RadonFiles.getPath("shaders/skybox/prefilter_map.frag");
    private static final Path ENVIRONMENT_VERTEX_SHADER_PATH = RadonFiles.getPath("shaders/skybox/equirect_to_cubemap.vert");
    private static final Path ENVIRONMENT_FRAGMENT_SHADER_PATH = RadonFiles.getPath("shaders/skybox/equirect_to_cubemap.frag");
    private static final int QUAD_INDEX_COUNT = 6;
    private static final int CUBE_INDEX_COUNT = 36;
    private static final boolean HDR_TEXTURE_FLIP_Y = true;


    private final GLContext context;
    // Framebuffer
    private GLFramebuffer framebuffer;
    // Shaders
    private GLShaderProgram environmentMapShader;
    private GLShaderProgram irradianceShader;
    private GLShaderProgram prefilterShader;
    private GLShaderProgram brdfShader;
    // Vertex data
    private GLVertexArray quadVAO;
    private GLBuffer quadVertexBuffer;
    private GLBuffer quadIndexBuffer;
    private GLVertexArray cubeVAO;
    private GLBuffer cubeVertexBuffer;
    private GLBuffer cubeIndexBuffer;

    public GLSkyboxPBRTextureFactory(GLContext context) {
        this.context = requireNonNull(context);
    }

    @Override
    protected void free() {
        Resource.release(framebuffer);
        Resource.release(brdfShader);
        Resource.release(irradianceShader);
        Resource.release(quadVAO);
        Resource.release(quadVertexBuffer);
        Resource.release(quadIndexBuffer);
        Resource.release(cubeVAO);
        Resource.release(cubeVertexBuffer);
        Resource.release(cubeIndexBuffer);
    }

    @Override
    public Texture2D createBRDFTexture(int size) {

        GLTexture2D brdf = createNewBRDFTexture(size);

        bakeBRDFTexture(brdf, size);

        return brdf;
    }

    @Override
    public Cubemap createEnvironmentMap(Path hdrTexturePath, int size, PixelFormat pixelFormat) {

        Texture2D hdrTexture = GraphicsFactory.get().newTexture2D(hdrTexturePath, pixelFormat, HDR_TEXTURE_FLIP_Y);

        SkyboxHelper.setSkyboxTextureSamplerParameters(hdrTexture);

        GLCubemap environmentTexture = createNewEnvironmentTexture(size);

        bakeEnvironmentMap((GLTexture2D) hdrTexture, environmentTexture, size);

        return environmentTexture;
    }

    @Override
    public Cubemap createIrradianceMap(Cubemap environmentMap, int size) {

        GLCubemap irradianceTexture = createNewIrradianceTexture(size);

        bakeIrradianceMap((GLCubemap) environmentMap, irradianceTexture, size);

        return irradianceTexture;
    }

    @Override
    public Cubemap createPrefilterMap(Cubemap environmentMap, int size, float maxLOD) {

        final int mipLevels = Math.round(maxLOD) + 1;

        Cubemap prefilterTexture = createNewPrefilterTexture(size, mipLevels);

        // Run a quasi monte-carlo simulation on the environment lighting to create a prefilter (cube)map.
        bakePrefilterMap(environmentMap, prefilterTexture, size, mipLevels);

        return prefilterTexture;
    }

    private void bakeEnvironmentMap(GLTexture2D equirectangularTexture, GLCubemap environmentMap, int size) {

        GLShaderProgram shader = environmentMapShader();

        shader.bind();

        shader.uniformSampler(EQUIRECTANGULAR_MAP_UNIFORM_NAME, equirectangularTexture, 0);

        renderCubemap(environmentMap, shader, size, 0);

        shader.unbind();

        environmentMap.generateMipmaps();
    }

    private void bakeIrradianceMap(GLCubemap environmentMap, GLCubemap irradianceTexture, int size) {

        GLShaderProgram shader = irradianceShader();

        shader.bind();

        shader.uniformSampler(ENVIRONMENT_MAP_UNIFORM_NAME, environmentMap, 0);

        renderCubemap(irradianceTexture, shader, size, 0);

        shader.unbind();

        irradianceTexture.generateMipmaps();
    }

    private void bakePrefilterMap(Cubemap environmentMap, Cubemap prefilterTexture, int size, int mipLevels) {

        GLShaderProgram shader = prefilterShader();

        shader.bind();

        shader.uniformSampler(ENVIRONMENT_MAP_UNIFORM_NAME, (GLTexture) environmentMap, 0);

        shader.uniformInt("u_Resolution", environmentMap.faceSize());

        final int minMipLevel = mipLevels - 1;

        for (int mipLevel = 0; mipLevel <= minMipLevel; mipLevel++) {

            final int mipLevelSize = (int) (size * pow(0.5f, mipLevel));

            final float roughness = (float) mipLevel / (float) minMipLevel;

            shader.uniformFloat("u_Roughness", roughness);

            renderCubemap((GLCubemap) prefilterTexture, shader, mipLevelSize, mipLevel);
        }

        // Do not generate mipmaps after render!
        // TODO
        // prefilterTexture.generateMipmaps();

        shader.unbind();
    }

    private void bakeBRDFTexture(GLTexture2D brdfTexture, int size) {

        framebuffer().attach(GL_COLOR_ATTACHMENT0, brdfTexture, 0);
        framebuffer().ensureComplete();

        framebuffer().bind();

        glDisable(GL_DEPTH_TEST);
        glViewport(0, 0, size, size);
        glClear(GL_COLOR_BUFFER_BIT);

        GLShaderProgram shader = brdfShader();
        GLVertexArray quadVAO = quadVAO();

        shader.bind();
        quadVAO.bind();

        glDrawElements(GL_TRIANGLES, QUAD_INDEX_COUNT, GL_UNSIGNED_INT, NULL);

        quadVAO.unbind();
        shader.unbind();

        framebuffer().detach(GL_COLOR_ATTACHMENT0);

        framebuffer().unbind();
    }

    private void renderCubemap(GLCubemap cubemap, GLShaderProgram shader, int size, int mipmapLevel) {

        Cubemap.Face[] faces = Cubemap.Face.values();

        Matrix4fc projectionMatrix = getProjectionMatrix();

        Matrix4f[] viewMatrices = getViewMatrices();

        GLVertexArray cubeVAO = cubeVAO();

        cubeVAO.bind();

        // cubemap.bind();

        framebuffer().bind();

        glDisable(GL_DEPTH_TEST);
        glViewport(0, 0, size, size);

        int f = GL_TEXTURE_CUBE_MAP_POSITIVE_X;

        for (int i = 0; i < faces.length; i++) {

            Matrix4f viewMatrix = viewMatrices[i];

            Matrix4f projectionViewMatrix = projectionMatrix.mul(viewMatrix, new Matrix4f());

            shader.uniformMatrix4f(PROJECTION_VIEW_MATRIX_UNIFORM_NAME, false, projectionViewMatrix);

            framebuffer().bind();

            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, f, cubemap.handle(), mipmapLevel);

            f++;

            // framebuffer().attach(GL_COLOR_ATTACHMENT0, cubemap, faces[i], mipmapLevel);
            // framebuffer().ensureComplete();

            glDrawElements(GL_TRIANGLES, CUBE_INDEX_COUNT, GL_UNSIGNED_INT, NULL);

        }
        glFinish();

        // framebuffer().detach(GL_COLOR_ATTACHMENT0);

        framebuffer().unbind();

        // cubemap.unbind();

        cubeVAO.unbind();
    }

    private Matrix4f[] getViewMatrices() {
        return new Matrix4f[]{
                new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f),
                new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f),
                new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f),
                new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, -1.0f),
                new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, -1.0f, 0.0f),
                new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, -1.0f, 0.0f)
        };
    }

    private Matrix4fc getProjectionMatrix() {
        return new Matrix4f().perspective(radians(90.0f), 1.0f, 0.1f, 100.0f);
    }

    private GLCubemap createNewEnvironmentTexture(int size) {

        GLCubemap environmentTexture = new GLCubemap(context);

        environmentTexture.allocate(4, size, PixelFormat.RGB16F);

        SkyboxHelper.setSkyboxTextureSamplerParameters(environmentTexture);

        return environmentTexture;
    }

    private GLCubemap createNewIrradianceTexture(int size) {

        GLCubemap irradianceTexture = new GLCubemap(context);

        irradianceTexture.allocate(4, size, PixelFormat.RGB16F);

        SkyboxHelper.setSkyboxTextureSamplerParameters(irradianceTexture);

        irradianceTexture.generateMipmaps();

        return irradianceTexture;
    }

    private Cubemap createNewPrefilterTexture(int size, int mipLevels) {

        GLCubemap prefilterTexture = new GLCubemap(context);

        prefilterTexture.allocate(mipLevels, size, PixelFormat.RGB16F);

        SkyboxHelper.setSkyboxTextureSamplerParameters(prefilterTexture);

        prefilterTexture.generateMipmaps();

        return prefilterTexture;
    }

    private GLTexture2D createNewBRDFTexture(int size) {

        GLTexture2D brdf = new GLTexture2D(context);

        brdf.allocate(1, size, size, GL_RG16F);

        return SkyboxHelper.setSkyboxTextureSamplerParameters(brdf);
    }

    private GLShaderProgram environmentMapShader() {
        if (environmentMapShader == null) {
            environmentMapShader = createEnvironmentMapShader();
        }
        return environmentMapShader;
    }

    private GLShaderProgram createEnvironmentMapShader() {
        return new GLShaderProgram(context, "Equirectangular to cubemap Shader")
                .attach(new GLShader(context, VERTEX_STAGE).source(ENVIRONMENT_VERTEX_SHADER_PATH).compile())
                .attach(new GLShader(context, FRAGMENT_STAGE).source(ENVIRONMENT_FRAGMENT_SHADER_PATH).compile())
                .link();
    }

    private GLShaderProgram irradianceShader() {
        if (irradianceShader == null) {
            irradianceShader = createIrradianceShader();
        }
        return irradianceShader;
    }

    private GLShaderProgram createIrradianceShader() {
        return new GLShaderProgram(context, "Irradiance Shader")
                .attach(new GLShader(context, VERTEX_STAGE).source(IRRADIANCE_VERTEX_SHADER_PATH))
                .attach(new GLShader(context, FRAGMENT_STAGE).source(IRRADIANCE_FRAGMENT_SHADER_PATH))
                .link();
    }

    private GLShaderProgram prefilterShader() {
        if (prefilterShader == null) {
            prefilterShader = createPrefilterShader();
        }
        return prefilterShader;
    }

    private GLShaderProgram createPrefilterShader() {
        return new GLShaderProgram(context, "Prefilter Shader")
                .attach(new GLShader(context, VERTEX_STAGE).source(PREFILTER_VERTEX_SHADER_PATH))
                .attach(new GLShader(context, FRAGMENT_STAGE).source(PREFILTER_FRAGMENT_SHADER_PATH))
                .link();
    }

    private GLShaderProgram brdfShader() {
        if (brdfShader == null) {
            brdfShader = createBRDFShaderProgram();
        }
        return brdfShader;
    }

    private GLShaderProgram createBRDFShaderProgram() {
        return new GLShaderProgram(context, "BRDF Shader")
                .attach(new GLShader(context, VERTEX_STAGE).source(BRDF_VERTEX_SHADER_PATH))
                .attach(new GLShader(context, FRAGMENT_STAGE).source(BRDF_FRAGMENT_SHADER_PATH))
                .link();
    }

    private GLVertexArray cubeVAO() {
        if (cubeVAO == null) {
            cubeVAO = createCubeVAO();
        }
        return cubeVAO;
    }

    private GLVertexArray createCubeVAO() {
        Mesh cubeMesh = StaticMesh.cube();
        cubeVertexBuffer = new GLBuffer(context).name("Cube VertexBuffer");
        cubeIndexBuffer = new GLBuffer(context).name("Cube IndexBuffer");
        return createVertexArray(cubeMesh, cubeVertexBuffer, cubeIndexBuffer);
    }

    private GLVertexArray quadVAO() {
        if (quadVAO == null) {
            quadVAO = createQuadVAO();
        }
        return quadVAO;
    }

    private GLVertexArray createQuadVAO() {
        Mesh quadMesh = MeshManager.get().get(QUAD_MESH_NAME);
        quadVertexBuffer = new GLBuffer(context).name("Quad VertexBuffer");
        quadIndexBuffer = new GLBuffer(context).name("Quad IndexBuffer");
        return createVertexArray(quadMesh, quadVertexBuffer, quadIndexBuffer);
    }

    private GLVertexArray createVertexArray(Mesh mesh, GLBuffer vertexBuffer, GLBuffer indexBuffer) {

        GLVertexArray vertexArray = new GLVertexArray(context);

        vertexBuffer.data(mesh.vertexData());
        vertexArray.addVertexBuffer(0, VERTEX_LAYOUT_3D.attributeList(0), vertexBuffer);

        indexBuffer.data(mesh.indexData());
        vertexArray.setIndexBuffer(indexBuffer);

        return vertexArray;
    }

    private GLFramebuffer framebuffer() {
        if (framebuffer == null) {
            framebuffer = createFramebuffer();
        }
        return framebuffer;
    }

    private GLFramebuffer createFramebuffer() {

        GLFramebuffer framebuffer = new GLFramebuffer(context);

        framebuffer.freeAttachmentsOnRelease(true);

        return framebuffer;
    }
}

package radon.engine.graphics.opengl.rendering.renderers.data;

import radon.engine.graphics.opengl.GLContext;
import radon.engine.graphics.opengl.buffers.GLBuffer;
import radon.engine.graphics.opengl.vertex.GLVertexArray;
import radon.engine.meshes.MeshManager;
import radon.engine.meshes.StaticMesh;
import radon.engine.meshes.vertices.VertexLayout;

import static radon.engine.meshes.vertices.VertexLayouts.VERTEX_LAYOUT_3D_INDIRECT;

public class GLStaticRenderData extends GLRenderData {

    public GLStaticRenderData(GLContext context) {
        super(context);
    }

    @Override
    protected GLBuffer initVertexBuffer() {
        return MeshManager.get().storageHandler(StaticMesh.class).vertexBuffer();
    }

    @Override
    protected GLBuffer initIndexBuffer() {
        return MeshManager.get().storageHandler(StaticMesh.class).indexBuffer();
    }

    @Override
    protected int getStride() {
        return StaticMesh.VERTEX_DATA_SIZE;
    }

    @Override
    protected GLVertexArray initVertexArray() {

        GLVertexArray vertexArray = new GLVertexArray(context());

        VertexLayout vertexLayout = VERTEX_LAYOUT_3D_INDIRECT;

        for (int i = 0; i < vertexLayout.bindings(); i++) {
            vertexArray.setVertexAttributes(i, vertexLayout.attributeList(i));
        }

        return vertexArray;
    }

    @Override
    protected GLBuffer initInstanceBuffer() {
        return new GLBuffer(context()).name("INSTANCE VERTEX BUFFER");
    }
}

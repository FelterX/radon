package radon.engine.graphics.opengl.vertex;

import radon.engine.graphics.opengl.GLContext;
import radon.engine.graphics.opengl.GLObject;
import radon.engine.graphics.opengl.buffers.GLBuffer;
import radon.engine.meshes.vertices.VertexAttribute;
import radon.engine.meshes.vertices.VertexAttributeList;
import radon.engine.meshes.vertices.VertexAttributeList.VertexAttributeIterator;

import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL45.*;
import static radon.engine.meshes.vertices.VertexAttribute.MATRIX4F;
import static radon.engine.util.types.DataType.FLOAT32_SIZEOF;

public final class GLVertexArray extends GLObject {

    public GLVertexArray(GLContext context) {
        super(context, glCreateVertexArrays());
    }

    public void setIndexBuffer(GLBuffer indexBuffer) {
        glVertexArrayElementBuffer(handle(), indexBuffer == null ? NULL : indexBuffer.handle());
    }

    public void setVertexBuffer(int binding, GLBuffer vertexBuffer, int stride) {
        glVertexArrayVertexBuffer(handle(), binding, vertexBuffer.handle(), 0, stride);
    }

    public void setVertexAttributes(int binding, VertexAttributeList attributes) {
        VertexAttributeIterator it = attributes.iterator();
        while (it.hasNext()) {
            setVertexAttribute(binding, it.next(), it.location(), it.offset(), attributes.stride());
        }
        glVertexArrayBindingDivisor(handle(), binding, attributes.instanced() ? 1 : 0);
    }

    public void addVertexBuffer(int binding, VertexAttributeList attributes, GLBuffer vertexBuffer) {

        glVertexArrayVertexBuffer(handle(), binding, vertexBuffer.handle(), 0, attributes.stride());

        VertexAttributeIterator it = attributes.iterator();

        while (it.hasNext()) {
            setVertexAttribute(binding, it.next(), it.location(), it.offset(), attributes.stride());
        }

        glVertexArrayBindingDivisor(handle(), binding, attributes.instanced() ? 1 : 0);
    }

    public void bind() {
        glBindVertexArray(handle());
    }

    public void unbind() {
        glBindVertexArray(0);
    }

    private void setVertexAttribute(int binding, VertexAttribute attribute, int location, int offset, int stride) {

        if (attribute.dataType().decimal()) {
            setAttributef(binding, location, attribute, offset);
        } else {
            setAttributei(binding, location, attribute, offset);
        }
    }

    private void setAttributef(int binding, int location, VertexAttribute attribute, int offset) {

        if (attribute == MATRIX4F) {

            final int dataType = GL_FLOAT;

            glEnableVertexArrayAttrib(handle(), location);
            glVertexArrayAttribBinding(handle(), location, binding);
            glVertexArrayAttribFormat(handle(), location, 4, dataType, false, offset);

            glEnableVertexArrayAttrib(handle(), location + 1);
            glVertexArrayAttribBinding(handle(), location + 1, binding);
            glVertexArrayAttribFormat(handle(), location + 1, 4, dataType, false, offset + 4 * FLOAT32_SIZEOF);

            glEnableVertexArrayAttrib(handle(), location + 2);
            glVertexArrayAttribBinding(handle(), location + 2, binding);
            glVertexArrayAttribFormat(handle(), location + 2, 4, dataType, false, offset + 8 * FLOAT32_SIZEOF);

            glEnableVertexArrayAttrib(handle(), location + 3);
            glVertexArrayAttribBinding(handle(), location + 3, binding);
            glVertexArrayAttribFormat(handle(), location + 3, 4, dataType, false, offset + 12 * FLOAT32_SIZEOF);

        } else {

            glEnableVertexArrayAttrib(handle(), location);
            glVertexArrayAttribBinding(handle(), location, binding);
            glVertexArrayAttribFormat(handle(), location, attribute.size(), mapToAPI(attribute.dataType()), false, offset);
        }
    }

    private void setAttributei(int binding, int location, VertexAttribute attribute, int offset) {
        glEnableVertexArrayAttrib(handle(), location);
        glVertexArrayAttribBinding(handle(), location, binding);
        glVertexArrayAttribIFormat(handle(), location, attribute.size(), mapToAPI(attribute.dataType()), offset);
    }

    @Override
    public void free() {
        glDeleteVertexArrays(handle());
        setHandle(NULL);
    }
}

package radon.engine.meshes.models;


import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMatrix4x4;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIVector3D;
import radon.engine.meshes.Mesh;
import radon.engine.meshes.StaticMesh;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static java.util.Objects.requireNonNull;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static radon.engine.util.Asserts.assertTrue;
import static radon.engine.util.types.DataType.UINT32_SIZEOF;

public abstract class AssimpLoader {

    protected ByteBuffer getIndices(AIMesh aiMesh) {

        final int numFaces = aiMesh.mNumFaces();

        if (numFaces == 0) {
            return null;
        }

        ByteBuffer indices = memAlloc(numFaces * 3 * UINT32_SIZEOF);

        AIFace.Buffer aiFaces = aiMesh.mFaces();

        for (int i = 0; i < numFaces; i++) {

            AIFace aiFace = aiFaces.get(i);

            IntBuffer faceIndices = aiFace.mIndices();

            assertTrue(faceIndices.remaining() == 3);

            for (int j = faceIndices.position(); j < faceIndices.remaining(); j++) {
                indices.putInt(faceIndices.get(j));
            }
        }

        return indices.rewind();
    }

    protected Matrix4fc matrix4fc(AIMatrix4x4 aiMatrix4) {
        return new Matrix4f(
                aiMatrix4.a1(), aiMatrix4.b1(), aiMatrix4.c1(), aiMatrix4.d1(),
                aiMatrix4.a2(), aiMatrix4.b2(), aiMatrix4.c2(), aiMatrix4.d2(),
                aiMatrix4.a3(), aiMatrix4.b3(), aiMatrix4.c3(), aiMatrix4.d3(),
                aiMatrix4.a4(), aiMatrix4.b4(), aiMatrix4.c4(), aiMatrix4.d4()
        );
    }

    protected void processTexCoordsAttribute(AIMesh aiMesh, StaticVertexHandler handler, ByteBuffer vertices, int stride) {

        AIVector3D.Buffer textureCoordinates = aiMesh.mTextureCoords(0);

        if (textureCoordinates == null) {
            return;
            // throw new IllegalStateException("Number of texture coordinates is zero");
        }

        int offset = StaticMesh.VERTEX_TEXCOORDS_OFFSET;

        Vector2f texCoords = new Vector2f();

        for (int i = 0; i < textureCoordinates.remaining(); i++) {

            AIVector3D aiTexCoords = textureCoordinates.get(i);

            texCoords.set(aiTexCoords.x(), aiTexCoords.y());

            handler.mapTextureCoords(texCoords).get(offset, vertices);

            offset += stride;
        }

    }

    protected void processNormalAttribute(AIMesh aiMesh, StaticVertexHandler handler, ByteBuffer vertices, int stride) {

        AIVector3D.Buffer normals = requireNonNull(aiMesh.mNormals());

        if (normals.remaining() == 0) {
            throw new IllegalStateException("Number of normals is zero");
        }

        int offset = Mesh.VERTEX_NORMAL_OFFSET;

        Vector3f normal = new Vector3f();

        for (int i = 0; i < normals.remaining(); i++) {

            AIVector3D aiNormal = normals.get(i);

            normal.set(aiNormal.x(), aiNormal.y(), aiNormal.z());

            handler.mapNormal(normal).get(offset, vertices);

            offset += stride;
        }

    }

    protected void processPositionAttribute(AIMesh aiMesh, StaticVertexHandler handler, ByteBuffer vertices, int stride) {

        if(aiMesh.mNumVertices() == 0) {
            throw new IllegalStateException("Number of positions is zero");
        }

        AIVector3D.Buffer positions = aiMesh.mVertices();

        int offset = 0;

        Vector3f position = new Vector3f();

        for(int i = 0; i < positions.remaining(); i++) {

            AIVector3D aiPosition = positions.get(i);

            position.set(aiPosition.x(), aiPosition.y(), aiPosition.z());

            handler.mapPosition(position).get(offset, vertices);

            offset += stride;
        }
    }
}

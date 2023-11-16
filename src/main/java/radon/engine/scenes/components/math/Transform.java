package radon.engine.scenes.components.math;

import org.joml.*;
import radon.engine.logging.Log;
import radon.engine.scenes.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class Transform extends Component<Transform> {
    private Transform parent;
    private Vector3f position;
    private Matrix4f rotation;
    private Vector3f scale;
    private Matrix4f modelMatrix;
    private Matrix4f normalMatrix; // Use Matrix4f to avoid alignment issues in shaders
    private List<Transform> children;
    private boolean modified;


    private Transform() {

    }

    @Override
    protected void init() {
        super.init();
        position = new Vector3f();
        rotation = new Matrix4f();
        scale = new Vector3f(1.0f);
        modelMatrix = new Matrix4f();
        normalMatrix = new Matrix4f();
        children = new ArrayList<>(0);
        identity();
    }

    public Transform identity() {
        assertNotDeleted();

        updateChildrenPosition(0, 0, 0);
        updateChildrenScale(1, 1, 1);
        updateChildrenRotation(0, 0, 0, 0);

        modelMatrix.identity();
        normalMatrix.identity();
        rotation.identity();

        modify();

        return this;
    }

    public boolean modified() {
        assertNotDeleted();
        return modified;
    }

    public Vector3fc position() {
        assertNotDeleted();
        return position;
    }

    public Transform position(float x, float y, float z) {
        assertNotDeleted();
        if(enabled()) {
            updateChildrenPosition(x, y, z);
            position.set(x, y, z);
            modify();
        }
        return this;
    }


    public Transform position(Vector3fc position) {
        assertNotDeleted();
        return position(position.x(), position.y(), position.z());
    }

    public Transform translate(float x, float y, float z) {
        assertNotDeleted();
        if(enabled()) {
            updateChildrenPosition(position.x + x, position.y + y, position.z + z);
            position.add(x, y, z);
            modify();
        }
        return this;
    }

    public Transform translate(Vector3fc translation) {
        assertNotDeleted();
        return translate(translation.x(), translation.y(), translation.z());
    }

    public Vector3fc scale() {
        assertNotDeleted();
        return scale;
    }

    public Transform scale(float x, float y, float z) {
        assertNotDeleted();
        if(enabled()) {
            updateChildrenScale(x, y, z);
            scale.set(x, y, z);
            modify();
        }
        return this;
    }

    public Transform scale(Vector3fc scale) {
        assertNotDeleted();
        return scale(scale.x(), scale.y(), scale.z());
    }

    public Transform scale(float xyz) {
        assertNotDeleted();
        return scale(xyz, xyz, xyz);
    }

    public Transform scaleX(float scaleX) {
        assertNotDeleted();
        return scale(scaleX, scale.y, scale.z);
    }

    public Transform scaleY(float scaleY) {
        assertNotDeleted();
        return scale(scale.x, scaleY, scale.z);
    }

    public Transform scaleZ(float scaleZ) {
        assertNotDeleted();
        return scale(scale.x, scale.y, scaleZ);
    }

    public Vector3f euler() {
        assertNotDeleted();
        return rotation.getEulerAnglesZYX(new Vector3f());
    }

    public Quaternionf rotation() {
        assertNotDeleted();
        return rotation.getNormalizedRotation(new Quaternionf());
    }

    public float angle() {
        assertNotDeleted();
        return rotation().angle();
    }

    public Transform resetRotation() {
        assertNotDeleted();

        if(enabled()) {

            rotation.identity();

            modify();
        }

        return this;
    }

    public Transform rotate(float radians, float x, float y, float z) {
        assertNotDeleted();
        if(enabled()) {
            updateChildrenRotation(radians, x, y, z);
            rotation.rotation(radians, x, y, z);
            modify();
        }
        return this;
    }

    public Transform rotate(float radians, Vector3fc axis) {
        assertNotDeleted();
        return rotate(radians, axis.x(), axis.y(), axis.z());
    }

    public Transform rotate(AxisAngle4f rotationAxis) {
        assertNotDeleted();
        return rotate(rotationAxis.angle, rotationAxis.x, rotationAxis.y, rotationAxis.z);
    }

    public Transform rotate(Quaternionfc rotation) {
        assertNotDeleted();
        return rotate(rotation.angle(), rotation.x(), rotation.y(), rotation.z());
    }

    public Transform rotateX(float radians) {
        assertNotDeleted();
        if(enabled()) {
            updateChildrenRotation(radians, 1, 0, 0);
            rotation.rotationX(radians);
            modify();
        }
        return this;
    }

    public Transform rotateY(float radians) {
        assertNotDeleted();
        if(enabled()) {
            updateChildrenRotation(radians, 0, 1, 0);
            rotation.rotationY(radians);
            modify();
        }
        return this;
    }

    public Transform rotateZ(float radians) {
        assertNotDeleted();
        if(enabled()) {
            updateChildrenRotation(radians, 0, 0, 1);
            rotation.rotationZ(radians);
            modify();
        }
        return this;
    }

    public Transform rotateAround(float radians, float rx, float ry, float rz, float x0, float y0, float z0) {

        final float x = (x0 - position.x) * scale.x;
        final float y = (y0 - position.y) * scale.y;
        final float z = (z0 - position.z) * scale.z;

        rotation.translate(x, y, z).rotate(radians, rx, ry, rz).translate(-x, -y, -z);

        modify();

        return this;
    }

    public Transform rotateAroundX(float radians, float x0, float y0, float z0) {
        return rotateAround(radians, 1, 0, 0, x0, y0, z0);
    }

    public Transform rotateAroundY(float radians, float x0, float y0, float z0) {
        return rotateAround(radians, 0, 1, 0, x0, y0, z0);
    }

    public Transform rotateAroundZ(float radians, float x0, float y0, float z0) {
        return rotateAround(radians, 0, 0, 1, x0, y0, z0);
    }

    public Transform transformation(Matrix4fc transformation) {
        Vector3f vector = new Vector3f();
        Quaternionf quaternion = new Quaternionf();
        transformation.getTranslation(vector);
        position(vector);
        transformation.getScale(vector);
        scale(vector);
        transformation.getUnnormalizedRotation(quaternion);
        rotate(quaternion);
        return this;
    }

    public Transform lookAt(float x, float y, float z) {
        assertNotDeleted();

        rotation.setLookAt(position.x, position.y, position.z, x, y, z, 0, 1, 0);

        return this;
    }

    public Matrix4fc modelMatrix() {
        assertNotDeleted();
        return modelMatrix;
    }

    public Matrix4fc normalMatrix() {
        assertNotDeleted();
        return normalMatrix;
    }

    public Transform parent() {
        assertNotDeleted();
        return parent;
    }

    public Transform child(int index) {
        return children.get(index);
    }

    public boolean addChild(Transform child) {
        assertNotDeleted();

        if(child == null) {
            Log.error("Cannot add a null child");
            return false;
        }

        if(child.parent == this) {
            Log.trace("The given transform is already a child of this transform");
            return false;
        }

        if(child.parent != null) {
            Log.error("The given transform already has a parent");
            return false;
        }

        if(child.scene() != scene()) {
            Log.error("Cannot add a transform child from another scene");
            return false;
        }

        child.parent = this;

        return children.add(child);
    }

    public boolean hasChild(Transform child) {
        assertNotDeleted();

        if(child == null) {
            return false;
        }

        return child.parent == this;
    }

    public boolean removeChild(Transform child) {
        assertNotDeleted();

        if(!hasChild(child)) {
            return false;
        }

        return children.remove(child);
    }

    public void removeAllChildren() {
        for(Transform child : children) {
            child.parent = null;
        }
        children.clear();
    }

    public Stream<Transform> children() {
        assertNotDeleted();
        return children.stream();
    }

    public void modify() {
        modified = true;
    }

    @Override
    protected TransformManager manager() {
        return (TransformManager) super.manager();
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    @Override
    protected Transform self() {
        return this;
    }

    @Override
    protected void onDestroy() {

        if(parent != null) {
            parent.removeChild(this);
        }
        parent = null;

        removeAllChildren();
        children = null;

        position = null;
        rotation = null;
        scale = null;
        modelMatrix = null;
        normalMatrix = null;
    }

    @Override
    public Class<? extends Component> type() {
        return Transform.class;
    }


    void update() {

        modelMatrix.translation(position).mulAffine(rotation).scale(scale);

        // normalMatrix = transpose(inverse(mat3(model)))
        // normalMatrix.set(modelMatrix).invert().transpose();
        // normalMatrix._m30(0.0f)._m31(0.0f)._m32(0.0f);
        modified = false;
    }

    private void updateChildrenPosition(float newX, float newY, float newZ) {
        if(children.isEmpty()) {
            return;
        }

        final float deltaX = newX - position.x;
        final float deltaY = newY - position.y;
        final float deltaZ = newZ - position.z;

        for(Transform child : children) {
            if(child.enabled()) {
                child.translate(deltaX, deltaY, deltaZ);
            }
        }
    }

    private void updateChildrenScale(float newX, float newY, float newZ) {
        if(children.isEmpty()) {
            return;
        }

        final float deltaX = newX - scale.x;
        final float deltaY = newY - scale.y;
        final float deltaZ = newZ - scale.z;

        for(Transform child : children) {
            if(child.enabled()) {
                final Vector3fc s = child.scale;
                child.scale(s.x() + deltaX, s.y() + deltaY, s.z() + deltaZ);
            }
        }
    }

    private void updateChildrenRotation(float radians, float newX, float newY, float newZ) {
        if(children.isEmpty()) {
            return;
        }

        for(Transform child : children) {
            if(child.enabled()) {
                child.rotate(radians, newX, newY, newZ);
            }
        }
    }
}
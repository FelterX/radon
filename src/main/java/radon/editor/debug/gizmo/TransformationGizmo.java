package radon.editor.debug.gizmo;

import imgui.ImVec2;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.extension.imguizmo.flag.Mode;
import imgui.extension.imguizmo.flag.Operation;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import radon.editor.core.RadonEditor;
import radon.editor.gui.GameViewport;
import radon.engine.input.Input;
import radon.engine.input.Key;
import radon.engine.scenes.Camera;
import radon.engine.scenes.Entity;
import radon.engine.scenes.SceneManager;
import radon.engine.scenes.components.math.Transform;

public class TransformationGizmo extends Gizmo {

    private static final float[] cameraView = {
            1.f, 0.f, 0.f, 0.f,
            0.f, 1.f, 0.f, 0.f,
            0.f, 0.f, 1.f, 0.f,
            0.f, 0.f, 0.f, 1.f
    };

    public static int operation = Operation.TRANSLATE;

    protected TransformationGizmo() {
    }

    public static void setOperation(int op) {
        operation = op;
    }

    public static boolean update(ImVec2 size) {
        setupImGuizmo(size);
        checkOperations();

        calculateView();
        Matrix4f projection = new Matrix4f();
        float[] cameraProjection = new float[16];
        SceneManager.scene().camera().getProjectionMatrix(projection, Camera.ProjectionType.PERSPECTIVE).get(cameraProjection);
        float[] model = model();

        ImGuizmo.manipulate(cameraView, cameraProjection, model, operation, Mode.LOCAL);
        if (ImGuizmo.isUsing()) {
            float[] position = new float[3];
            float[] rotation = new float[3];
            float[] scale = new float[3];
            ImGuizmo.decomposeMatrixToComponents(model, position, rotation, scale);
            Vector3f pos = vec3(position);
            Vector3f rot = vec3(rotation);
            Vector3f sca = vec3(scale);

            Entity entity = RadonEditor.selected;

            Transform transform = entity.get(Transform.class);

            transform.position(pos);
           // transform.localRotation = rot;
            transform.scale(sca);

            return true;
        }

        return false;
    }

    private static void setupImGuizmo(ImVec2 size) {
        ImGuizmo.setOrthographic(false);
        ImGuizmo.setEnabled(true);
        ImGuizmo.setDrawList();

        Vector2f imagePosition = GameViewport.imagePosition;
        Vector2f imageSize = GameViewport.imageSize;
        Vector2f viewportPosition = GameViewport.position;
        ImGuizmo.setRect(viewportPosition.x + imagePosition.x, viewportPosition.y + imagePosition.y, imageSize.x, imageSize.y);
    }

    private static void checkOperations() {
        //if (!Viewport.ViewportFocused) return;

        if (Input.isKeyPressed(Key.KEY_T)) {
            operation = Operation.TRANSLATE;
        } else if (Input.isKeyPressed(Key.KEY_R)) {
            operation = Operation.ROTATE;
        } else if (Input.isKeyPressed(Key.KEY_S)) {
            operation = Operation.SCALE;
        }

        if (Input.isKeyPressed(Key.KEY_X)) {
            switch (operation) {
                case Operation.TRANSLATE -> operation = Operation.TRANSLATE_X;
                case Operation.ROTATE -> operation = Operation.ROTATE_X;
                case Operation.SCALE -> operation = Operation.SCALE_X;
                default -> operation = Operation.TRANSLATE;
            }
        } else if (Input.isKeyPressed(Key.KEY_Y)) {
            switch (operation) {
                case Operation.TRANSLATE -> operation = Operation.TRANSLATE_Y;
                case Operation.ROTATE -> operation = Operation.ROTATE_Y;
                case Operation.SCALE -> operation = Operation.SCALE_Y;
                default -> operation = Operation.TRANSLATE;
            }
        } else if (Input.isKeyPressed(Key.KEY_Z)) {
            switch (operation) {
                case Operation.TRANSLATE -> operation = Operation.TRANSLATE_Z;
                case Operation.ROTATE -> operation = Operation.ROTATE_Z;
                case Operation.SCALE -> operation = Operation.SCALE_Z;
                default -> operation = Operation.TRANSLATE;
            }
        } else {
            switch (operation) {
                case Operation.TRANSLATE_X, Operation.TRANSLATE_Y, Operation.TRANSLATE_Z ->
                        operation = Operation.TRANSLATE;
                case Operation.ROTATE_X, Operation.ROTATE_Y, Operation.ROTATE_Z -> operation = Operation.ROTATE;
                case Operation.SCALE_X, Operation.SCALE_Y, Operation.SCALE_Z -> operation = Operation.SCALE;
            }
        }
    }

    private static void calculateView() {
        Matrix4f view = new Matrix4f();
        SceneManager.scene().editorCamera().getViewMatrix(view);
        view.get(cameraView);
    }

    private static float[] model() {
        Entity entity = RadonEditor.selected;

        Transform transform = entity.get(Transform.class);

        float[] model = new float[16];
        float[] position = array(transform.position());
        float[] rotation = array(new Vector3f());
        float[] scale = array(transform.scale());
        ImGuizmo.recomposeMatrixFromComponents(model, position, rotation, scale);

        return model;
    }

    private static float[] emptyModel() {
        float[] model = new float[16];
        float[] position = new float[]{0, 0, 0};
        float[] rotation = new float[]{0, 0, 0};
        float[] scale = new float[]{1, 1, 1};
        ImGuizmo.recomposeMatrixFromComponents(model, position, rotation, scale);

        return model;
    }

    private static float[] array(Vector3fc vector) {
        return new float[]{vector.x(), vector.y(), vector.z()};
    }

    private static Vector3f vec3(float[] arr) {
        return new Vector3f(arr[0], arr[1], arr[2]);
    }

    @Override
    public void update() {

    }

    @Override
    public void onDestroy() {

    }
}

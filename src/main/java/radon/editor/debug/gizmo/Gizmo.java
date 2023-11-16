package radon.editor.debug.gizmo;

public abstract class Gizmo {

    public abstract void update();
    public abstract void onDestroy();

    public void destroy() {
        onDestroy();
        GizmoManager.gizmos.remove(this);
    }

}

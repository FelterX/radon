package radon.editor.gui;

import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.flag.ImGuiWindowFlags;
import radon.editor.core.RadonEditor;
import radon.engine.scenes.Entity;
import radon.engine.scenes.Scene;
import radon.engine.scenes.SceneManager;
import radon.engine.scenes.components.math.Transform;

import java.util.List;

public class SceneHierarchy extends EditorWindow {
    @Override
    public void onGui() {

        Scene scene = SceneManager.scene();

        ImGui.begin("Scene Hierarchy",
                ImGuiWindowFlags.NoMove);

        if (scene != null) {
            List<Entity> entities = scene.entities().toList();
            int[] index = {0};

            for (Entity entity : entities) {
                Transform transform = entity.get(Transform.class);
                if (transform != null) {
                    if (transform.parent() == null) {
                        treeNode(transform, index);
                    }
                } else {
                    ImGui.pushID(index[0]);

                    if (ImGui.selectable(entity.name(), RadonEditor.selected == entity)) {
                        RadonEditor.selected = entity;
                    }

                    ImGui.popID();
                }
            }
        }

        ImGui.end();
    }

    private void treeNode(Transform transform, int[] index) {

        ImGui.pushID(index[0]);
        Entity entity = transform.entity();
        boolean open;

        int flags  =
                ImGuiTreeNodeFlags.FramePadding |
                ImGuiTreeNodeFlags.OpenOnArrow |
                ImGuiTreeNodeFlags.SpanAvailWidth;

        if (RadonEditor.selected == entity)
            flags |= ImGuiTreeNodeFlags.Selected;

        open = ImGui.treeNodeEx(entity.name(), flags, entity.name());
        if (ImGui.isItemClicked()) {
            RadonEditor.selected = entity;
        }

        ImGui.popID();

        if (open) {
            List<Transform> children = transform.children().toList();
            for (Transform child : children) {
                treeNode(child, index);
            }

            ImGui.treePop();
        }

        index[0]++;
    }
}

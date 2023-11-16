package radon.editor.gui;

import imgui.ImGui;

public class MenuBar extends EditorWindow{
    @Override
    public void onGui() {
        ImGui.beginMenuBar();

        if (ImGui.beginMenu("File")) {
            ImGui.endMenu();
        }

        if (ImGui.beginMenu("View")) {
            ImGui.endMenu();
        }

        ImGui.endMenuBar();
    }
}

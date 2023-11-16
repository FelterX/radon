package radon.editor.gui;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;


public class FilesWindow extends EditorWindow {
    @Override
    public void onGui() {
        ImGui.begin("Project",
                ImGuiWindowFlags.NoMove);


        ImGui.end();
    }
}

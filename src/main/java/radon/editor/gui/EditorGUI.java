package radon.editor.gui;

import imgui.ImGui;
import imgui.type.ImString;

public class EditorGUI {

    public static boolean checkbox(String label, boolean displayValue){
        boolean newBoolean = displayValue;

        if (ImGui.checkbox(label, displayValue)) {
            newBoolean = !displayValue;
        }

        return newBoolean;
    }

    public static String inputString(String label, String displayValue) {
        String newString = displayValue;

        ImString outString = new ImString(displayValue, 256);
        if (ImGui.inputText(label, outString)) {
            newString = outString.get();
        }

        return newString;
    }

    private static float hoverTime = 0;
    public static void tooltip(String text) {
        if (ImGui.isItemHovered() && hoverTime > 0.75f) {
            ImGui.beginTooltip();
            ImGui.setTooltip(text);
            ImGui.endTooltip();
        } else if (!ImGui.isAnyItemHovered()) {
            hoverTime = 0;
        }
    }

}

package radon.editor.gui;


import imgui.ImGui;
import imgui.ImGuiWindowClass;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.internal.flag.ImGuiDockNodeFlags;
import imgui.internal.flag.ImGuiItemFlags;
import radon.engine.core.Radon;
import radon.engine.core.RadonFiles;
import radon.engine.graphics.GraphicsFactory;
import radon.engine.graphics.opengl.textures.GLTexture2D;
import radon.engine.images.PixelFormat;

public class ToolsWindow extends EditorWindow {
    private final GLTexture2D playIcon;
    private final GLTexture2D pauseIcon;
    private final GLTexture2D stopIcon;

    public ToolsWindow() {
        playIcon = (GLTexture2D) GraphicsFactory.get().newTexture2D(RadonFiles.getPath("textures/editor/icons/play.png"), PixelFormat.RGBA);
        pauseIcon = (GLTexture2D) GraphicsFactory.get().newTexture2D(RadonFiles.getPath("textures/editor/icons/pause.png"), PixelFormat.RGBA);
        stopIcon = (GLTexture2D) GraphicsFactory.get().newTexture2D(RadonFiles.getPath("textures/editor/icons/stop.png"), PixelFormat.RGBA);
    }

    @Override
    public void onGui() {


        ImGuiWindowClass window = new ImGuiWindowClass();

        window.addDockNodeFlagsOverrideSet(ImGuiDockNodeFlags.NoTabBar);
        window.addDockNodeFlagsOverrideSet(ImGuiDockNodeFlags.NoResize);
        window.addDockNodeFlagsOverrideSet(ImGuiDockNodeFlags.NoDocking);
        window.addDockNodeFlagsOverrideSet(ImGuiDockNodeFlags.NoDockingInCentralNode);
        window.addDockNodeFlagsOverrideSet(ImGuiDockNodeFlags.NoDockingOverMe);
        window.addDockNodeFlagsOverrideSet(ImGuiDockNodeFlags.NoDockingSplitMe);
        window.addDockNodeFlagsOverrideSet(ImGuiDockNodeFlags.NoDockingOverOther);
        window.addDockNodeFlagsOverrideSet(ImGuiDockNodeFlags.NoDockingSplitOther);
        ImGui.setNextWindowClass(window);

        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0, 0);
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 0, 0);
        ImGui.begin("Tools Window",
                ImGuiWindowFlags.NoScrollbar |
                        ImGuiWindowFlags.NoScrollWithMouse |
                        ImGuiWindowFlags.NoDecoration |
                        ImGuiWindowFlags.NoMove |
                        ImGuiWindowFlags.NoResize |
                        ImGuiWindowFlags.NoTitleBar |
                        ImGuiWindowFlags.NoNavFocus |
                        ImGuiWindowFlags.NoNav |
                        ImGuiWindowFlags.NoCollapse);

        if (toolButton(playIcon, Radon.isRuntime())) {
            if(Radon.startRuntime()){

            }
        }
        if (toolButton(pauseIcon, !Radon.isRuntime())) {

        }
        if (toolButton(stopIcon, !Radon.isRuntime())) {
            if(Radon.stopRuntime()){

            }
        }

        ImGui.popStyleVar();
        ImGui.popStyleVar();
        ImGui.end();
    }

    private boolean toolButton(GLTexture2D icon, boolean disabled) {
        boolean bool;

        ImGui.pushStyleColor(ImGuiCol.Button, 0, 0, 0, 0);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0, 0, 0, 0);

        if (disabled) {
            ImGui.pushStyleVar(ImGuiItemFlags.Disabled, 1);
            ImGui.pushStyleVar(ImGuiStyleVar.Alpha, ImGui.getStyle().getAlpha() * 0.5f);
            ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0, 0, 0, 0);
        }

        bool = ImGui.imageButton(icon.handle(), ImGui.getWindowSizeY(), ImGui.getWindowSizeY());
        ImGui.sameLine();

        ImGui.popStyleColor(2);
        if (disabled) {
            ImGui.popStyleColor();
            ImGui.popStyleVar(2);
        }

        return bool;
    }
}

package radon.editor.gui;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiWindowFlags;
import org.joml.Vector2f;
import radon.editor.core.RadonEditor;
import radon.editor.debug.gizmo.TransformationGizmo;
import radon.engine.core.Radon;
import radon.engine.graphics.opengl.rendering.GLRenderSystem;
import radon.engine.graphics.opengl.textures.GLTexture;
import radon.engine.graphics.rendering.RenderSystem;
import radon.engine.graphics.window.CursorType;
import radon.engine.graphics.window.Window;
import radon.engine.input.Input;
import radon.engine.input.Key;
import radon.engine.scenes.Camera;
import radon.engine.scenes.SceneManager;

import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static radon.engine.core.Time.deltaTime;
import static radon.engine.util.Maths.clamp;

public class GameViewport extends EditorWindow {

    public static Vector2f position = new Vector2f();
    public static Vector2f size = new Vector2f();
    public static Vector2f imageSize = new Vector2f();
    public static Vector2f imagePosition = new Vector2f();


    private float minSpeed = 0.5f, maxSpeed = 3.0f;
    private float moveSpeed = 0.5f, mouseSensitivity = 1.5f;
    private double oldMouseX = 0, oldMouseY = 0, newMouseX, newMouseY;
    private boolean locked = false;

    public GameViewport() {
    }

    @Override
    public void onGui() {

        ImGui.begin("Game Viewport",
                ImGuiWindowFlags.NoScrollbar |
                        ImGuiWindowFlags.NoScrollWithMouse |
                        ImGuiWindowFlags.NoMove);

        ImGui.setCursorPos(ImGui.getCursorPosX(), ImGui.getCursorPosY());


        ImVec2 s = getLargestSizeForViewport();
        ImVec2 p = getCenteredPositionForViewport(s);

        imageSize = new Vector2f(s.x, s.y);
        imagePosition = new Vector2f(p.x, p.y);

        ImGui.setCursorPos(p.x, p.y);
        GLRenderSystem renderSystem = (GLRenderSystem) RenderSystem.getInstance().getAPIRenderSystem();

        GLTexture texture = renderSystem.mainFramebuffer().get(GL_COLOR_ATTACHMENT0);
        ImGui.image(texture.handle(), s.x, s.y, 0, 1, 1, 0);

        ImVec2 pos = ImGui.getWindowPos();
        ImVec2 siz = ImGui.getWindowSize();
        Vector2f viewportPos = new Vector2f(pos.x, pos.y);
        Vector2f viewportSize = new Vector2f(siz.x, siz.y);
        position = viewportPos;
        size = viewportSize;

        if (!Radon.isRuntime()) {
            boolean usingTransformationGizmo = false;
            if (RadonEditor.selected != null) {
                usingTransformationGizmo = TransformationGizmo.update(s);
            }

            updateEditorCamera(usingTransformationGizmo);
        }

        ImGui.end();

    }

    private void updateEditorCamera(boolean usingGizmo) {
        Camera camera = SceneManager.scene().editorCamera();

        newMouseX = ImGui.getMousePosX();
        newMouseY = ImGui.getMousePosY();
        if (ImGui.isWindowFocused()) {
            //Keyboard

            float scroll = Input.scrollY();
            if (scroll != 0.0f) {
                if (!Input.isKeyPressed(Key.KEY_LEFT_CONTROL)) {
                    camera.move(scroll > 0.0f ? Camera.Direction.FORWARD : Camera.Direction.BACKWARD, moveSpeed * 20.0f * deltaTime());
                } else {
                    moveSpeed = clamp(minSpeed, maxSpeed, (moveSpeed + scroll * 10.0f * deltaTime()));
                    System.out.println(moveSpeed);
                }
            }

            //Mouse
            if (!usingGizmo) {
                if (ImGui.isWindowHovered() && (ImGui.isMouseClicked(1) || ImGui.isMouseClicked(2))) {
                    Window.get().cursorType(CursorType.DISABLED);
                    locked = true;
                }
                if (locked) {

                    if (ImGui.isMouseDown(1)) {
                        float dx = (float) (newMouseX - oldMouseX);
                        float dy = (float) (newMouseY - oldMouseY);

                        camera.pitch(camera.pitch() - dy * mouseSensitivity * deltaTime());
                        camera.pitch(clamp(-89, 89, camera.pitch()));

                        camera.yaw(camera.yaw() + dx * mouseSensitivity * deltaTime());
                    }else if (ImGui.isMouseDown(2)) {
                        float dx = (float) (newMouseX - oldMouseX);
                        if (dx != 0) {
                            camera.move(dx > 0.0f ? Camera.Direction.RIGHT : Camera.Direction.LEFT, moveSpeed * 17.0f * deltaTime());
                        }
                    }

                }
            }
        }
        if (locked && (ImGui.isMouseReleased(1) || ImGui.isMouseReleased(2))) {
            Window.get().cursorType(CursorType.NORMAL);
            locked = false;
        }
        oldMouseX = newMouseX;
        oldMouseY = newMouseY;
    }

    private ImVec2 getLargestSizeForViewport() {
        ImVec2 windowSize = new ImVec2();
        ImGui.getContentRegionAvail(windowSize);

        float aspectWidth = windowSize.x;
        float aspectHeight = aspectWidth / Window.get().aspect();
        if (aspectHeight > windowSize.y) {
            // We must switch to pillarbox mode
            aspectHeight = windowSize.y;
            aspectWidth = aspectHeight * Window.get().aspect();
        }

        return new ImVec2(aspectWidth, aspectHeight);
    }

    private ImVec2 getCenteredPositionForViewport(ImVec2 aspectSize) {
        ImVec2 windowSize = new ImVec2();
        ImGui.getContentRegionAvail(windowSize);

        float viewportX = (windowSize.x / 2.0f) - (aspectSize.x / 2.0f);
        float viewportY = (windowSize.y / 2.0f) - (aspectSize.y / 2.0f);

        return new ImVec2(viewportX + ImGui.getCursorPosX(), viewportY + ImGui.getCursorPosY());
    }
}

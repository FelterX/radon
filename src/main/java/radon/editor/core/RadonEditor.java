package radon.editor.core;

import radon.editor.gui.*;
import radon.engine.core.*;
import radon.engine.graphics.GraphicsFactory;
import radon.engine.graphics.opengl.textures.GLTexture2D;
import radon.engine.graphics.textures.Texture;
import radon.engine.images.PixelFormat;
import radon.engine.logging.Log;
import radon.engine.materials.MaterialFactory;
import radon.engine.materials.PhongMaterial;
import radon.engine.meshes.StaticMesh;
import radon.engine.meshes.views.StaticMeshView;
import radon.engine.scenes.Entity;
import radon.engine.scenes.Scene;
import radon.engine.scenes.components.math.Transform;
import radon.engine.scenes.components.meshes.StaticMeshInstance;
import radon.engine.scenes.components.sprites.SpriteInstance;
import radon.engine.sprites.Sprite;
import radon.engine.util.Color;
import radon.engine.util.geometry.Rect;
import radon.project.GameType;
import radon.project.Project;
import radon.project.serialization.Serializer;

import java.io.File;

public class RadonEditor extends RadonApplication {

    private static Project project;
    public static Entity selected;

    private ImGuiLayer imGuiLayer;

    private GameViewport gameViewport;
    private SceneHierarchy sceneHierarchy;
    private InspectorWindow inspectorWindow;
    private ToolsWindow toolsWindow;
    private FilesWindow filesWindow;

    private RadonEditor() {

        RadonConfigurationHelper.releaseConfiguration();
    }

    public static void main(String[] args) {
        openProject(args);
        Radon.launch(new RadonEditor());
    }

    private static void openProject(String[] args) {
        String projectPath = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--project") && i + 1 <= args.length) {
                projectPath = args[i + 1];
                break;
            }
        }

        if (projectPath != null) {
            File file = new File(projectPath);
            if (file.exists()) {
                project = Serializer.deserialize(projectPath);
            }
        }

        if (project == null) {
            project = new Project("UnnamedRadonProject", null, Radon.RADON_VERSION, GameType.THREE_DIMENSION);
        }
    }

    @Override
    protected void onStart(Scene scene) {

        Log.info("Editor started with project: " + project.getName());

        this.imGuiLayer = new ImGuiLayer();
        this.imGuiLayer.init();

        this.gameViewport = new GameViewport();
        this.sceneHierarchy = new SceneHierarchy();
        this.inspectorWindow = new InspectorWindow();
        this.toolsWindow = new ToolsWindow();
        this.filesWindow = new FilesWindow();

        Entity root = scene.newEntity("root");
        root.add(Transform.class);
        root.add(TestComp.class);

        Entity map = scene.newEntity("map");
        map.add(Transform.class);

        Entity floor = scene.newEntity("floor");
        floor.add(Transform.class);
        floor.get(Transform.class).position(0, -3, -10);
        floor.get(Transform.class).scale(10, 0.01f, 10);

        PhongMaterial material0 = new MaterialFactory<>(PhongMaterial.class).getMaterial("mat0", m -> {
            m.color(Color.colorRandom());
        });

        floor.add(StaticMeshInstance.class);
        floor.get(StaticMeshInstance.class).meshViews(new StaticMeshView(StaticMesh.cube(), material0));

        // root.get(Transform.class).addChild(map.get(Transform.class));
        map.get(Transform.class).addChild(floor.get(Transform.class));
    }

    @Override
    protected void onRenderEnd() {
        super.onRenderEnd();

        imGuiLayer.startFrame();

        gameViewport.onGui();
        sceneHierarchy.onGui();
        inspectorWindow.onGui();
        toolsWindow.onGui();
        filesWindow.onGui();

        imGuiLayer.endFrame();
    }

    @Override
    protected void onTerminate() {
        super.onTerminate();
        if (imGuiLayer != null)
            imGuiLayer.terminate();
    }
}

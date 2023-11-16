package radon.engine.example;

import radon.engine.core.Radon;
import radon.engine.core.RadonApplication;
import radon.engine.materials.MaterialFactory;
import radon.engine.materials.PhongMaterial;
import radon.engine.meshes.StaticMesh;
import radon.engine.meshes.views.StaticMeshView;
import radon.engine.scenes.Entity;
import radon.engine.scenes.Scene;
import radon.engine.scenes.components.math.Transform;
import radon.engine.scenes.components.meshes.StaticMeshInstance;
import radon.engine.util.Color;

public class Example extends RadonApplication {

    public static void main(String[] args) {
        Radon.launch(new Example());
    }

    @Override
    protected void onStart(Scene scene) {
        Entity e = scene.newEntity("camera");
        e.add(Transform.class);
        e.get(Transform.class).position(0, 0, -10);
        e.get(Transform.class).rotateX(0.14f);

        e.add(StaticMeshInstance.class);

        StaticMesh mesh = StaticMesh.cube();

        PhongMaterial material = new MaterialFactory<>(PhongMaterial.class).getMaterial("Test mat", m -> {
            m.color(Color.colorRandom());
        });

        e.get(StaticMeshInstance.class).meshViews(new StaticMeshView(mesh, material));
    }
}

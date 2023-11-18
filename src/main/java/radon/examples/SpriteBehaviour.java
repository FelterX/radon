package radon.examples;

import radon.engine.input.Input;
import radon.engine.input.Key;
import radon.engine.logging.Log;
import radon.engine.scenes.components.behaviours.Behaviour;
import radon.engine.scenes.components.math.Transform;

import static radon.engine.core.Time.deltaTime;

public class SpriteBehaviour extends Behaviour {



    @Override
    public void onUpdate() {
        super.onUpdate();


        if (Input.isKeyPressed(Key.KEY_W)) {
            entity().get(Transform.class).translate(0, 1 * deltaTime(), 0);

        }
        if (Input.isKeyPressed(Key.KEY_S)) {
            entity().get(Transform.class).translate(0, -1 * deltaTime(), 0);
        }
        if (Input.isKeyPressed(Key.KEY_A)) {
            entity().get(Transform.class).translate(-1 * deltaTime(), 0, 0);
        }
        if (Input.isKeyPressed(Key.KEY_D)) {
            entity().get(Transform.class).translate(1 * deltaTime(), 0, 0);
        }
    }
}

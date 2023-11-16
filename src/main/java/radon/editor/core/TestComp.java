package radon.editor.core;

import org.joml.Vector2f;
import org.joml.Vector3f;
import radon.engine.scenes.components.behaviours.Behaviour;
import radon.engine.util.Color;
import radon.engine.util.editor.Header;
import radon.engine.util.editor.RangeFloat;
import radon.engine.util.editor.RangeInt;
import radon.engine.util.editor.Tooltip;
import radon.engine.util.types.DataType;

public class TestComp extends Behaviour {


    @Header("Int")
    public int testInt0 = 0;
    @RangeInt(min = 0, max = 10)
    public int testInt1 = 0;

    @Header("Float")
    public float testFloat0 = 0.0f;
    @RangeFloat(min = 0.5f, max =  1.0f)
    public float testFloat1 = 0.0f;

    @Header("Boolean")
    public boolean testBoolean0 = false;

    @Header("String")
    public String testString0 = "";

    @Header("Vector2f")
    public Vector2f testVector2f0;

    @Header("Vector3f")
    public Vector3f testVector3f0;

    @Header("Color")
    public Color testColor0;

    @Header("Enum")
    @Tooltip("It's a simple test for check if GUI works")
    public DataType enumTest0;



}

package radon.engine.graphics.opengl.shaders;

public class UniformUtils {

    public static String uniformArrayElement(String uniformName, int index) {
        return uniformName + "[" + index + "]";
    }

    public static String uniformStructMember(String structName, String memberName) {
        return structName + "." + memberName;
    }

    private UniformUtils() {}
}

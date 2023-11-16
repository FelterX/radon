package radon.engine.util.editor;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(value = ExecuteGUIS.class)
public @interface ExecuteGUI {

    String value() default "GUI";

}
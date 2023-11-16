package radon.editor.gui;

import imgui.ImGui;
import imgui.flag.ImGuiColorEditFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImInt;
import imgui.type.ImString;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import radon.editor.core.RadonEditor;
import radon.engine.core.RadonFiles;
import radon.engine.graphics.GraphicsFactory;
import radon.engine.graphics.opengl.textures.GLTexture2D;
import radon.engine.images.PixelFormat;
import radon.engine.scenes.Component;
import radon.engine.scenes.Entity;
import radon.engine.scenes.components.math.Transform;
import radon.engine.util.Color;
import radon.engine.util.EnumUtils;
import radon.engine.util.editor.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class InspectorWindow extends EditorWindow {

    private final GLTexture2D defaultIcon;
    private final Map<Class<? extends Component>, GLTexture2D> icons;


    public InspectorWindow() {
        this.defaultIcon = loadIcon("transform");
        this.icons = new HashMap<>();

        loadIcons();
    }

    private static float[] toFloatArray(Vector3fc vector) {
        return new float[]{vector.x(), vector.y(), vector.z()};
    }

    private static Vector3f fromFloatArray(float[] array) {
        return new Vector3f(array[0], array[1], array[2]);
    }

    private void loadIcons() {
        icons.put(Transform.class, loadIcon("transform"));
    }

    private GLTexture2D loadIcon(String name) {
        return (GLTexture2D) GraphicsFactory.get().newTexture2D(RadonFiles.getPath("textures/editor/icons/components/" + name + ".png"), PixelFormat.RGBA);
    }

    @Override
    public void onGui() {
        ImGui.begin("Properties Window", ImGuiWindowFlags.NoMove);

        Entity entity = RadonEditor.selected;
        if (entity != null) {
            entity.enable(EditorGUI.checkbox("##active", entity.enabled()));
            ImGui.sameLine();
            ImGui.setNextItemWidth(ImGui.getContentRegionAvailX());
            entity.setName(EditorGUI.inputString("##name", entity.name()));

            Transform transform = entity.get(Transform.class);
            if (transform != null) {
                boolean variableUpdated = false;

                Vector3f position = (Vector3f) transform.position();
                Vector3f euler = transform.euler();
                Vector3f scale = (Vector3f) transform.scale();

                float[] p = {position.x, position.y, position.z};
                if (ImGui.dragFloat3("position", p, 0.1f)) {
                    position.set(p[0], p[1], p[2]);
                    variableUpdated = true;
                }

                float[] r = {euler.x, euler.y, euler.z};
                if (ImGui.dragFloat3("rotation", r, 0.1f)) {

                }

                float[] s = {scale.x, scale.y, scale.z};
                if (ImGui.dragFloat3("scale", s, 0.1f)) {
                    scale.set(s[0], s[1], s[2]);
                    variableUpdated = true;
                }


                if (variableUpdated) {
                    transform.modify();
                }
            }

            List<Component> components = entity.components().toList();
            for (Component c : components) {
                if (c.getClass() == Transform.class) continue;
                renderComponent(c, c.hashCode());
            }

        }
        ImGui.end();
    }

    private void renderComponent(Component comp, int id) {

        comp.enable(EditorGUI.checkbox("##enable", comp.enabled()));
        ImGui.sameLine();
        GLTexture2D icon = icons.getOrDefault(comp.getClass(), defaultIcon);
        ImGui.image(icon.handle(), 20, 20);
        ImGui.sameLine();

        String className = comp.getClass().getSimpleName();

        if (ImGui.treeNodeEx(id, ImGuiTreeNodeFlags.FramePadding | ImGuiTreeNodeFlags.SpanAvailWidth, className)) {
            try {
                Field[] fields = comp.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if (field.isAnnotationPresent(ExecuteGUI.class)) {
                        ExecuteGUI executeGUI = field.getAnnotation(ExecuteGUI.class);
                        comp.executeGui(executeGUI.value());
                    }
                    if (field.isAnnotationPresent(ExecuteGUIS.class)) {
                        ExecuteGUI[] guis = field.getAnnotation(ExecuteGUIS.class).value();
                        for (ExecuteGUI executeGUI : guis) {
                            comp.executeGui(executeGUI.value());
                        }
                    }


                    boolean variableUpdated = false;
                    boolean isPrivate = Modifier.isPrivate(field.getModifiers());
                    boolean isStatic = Modifier.isStatic(field.getModifiers());
                    if (isPrivate || isStatic || field.getModifiers() == 0) {
                        continue;
                    }

                    Class type = field.getType();
                    Object value = field.get(comp);
                    String name = field.getName();

                    for (Annotation annotation : field.getAnnotations()) {
                        if (annotation instanceof Header) {
                            Header header = (Header) annotation;
                            ImGui.text(header.value());
                            ImGui.spacing();
                            ImGui.separator();
                            ImGui.spacing();
                        } else if (annotation instanceof Divider) {
                            ImGui.spacing();
                            ImGui.separator();
                            ImGui.spacing();
                        }
                    }


                    if (field.isAnnotationPresent(HideInEditor.class)) {
                        continue;
                    }

                    if (type == int.class) {
                        int val = (int) value;
                        int[] imInt = {val};

                        if (field.isAnnotationPresent(RangeInt.class)) {
                            RangeInt anno = field.getAnnotation(RangeInt.class);
                            if (ImGui.sliderInt(name, imInt, anno.min(), anno.max())) {
                                field.set(comp, imInt[0]);
                                variableUpdated = true;
                            }
                        } else {
                            if (ImGui.dragInt(name, imInt)) {
                                field.set(comp, imInt[0]);
                                variableUpdated = true;
                            }
                        }
                    } else if (type == float.class) {
                        float val = (float) value;
                        float[] imFloat = {val};

                        if (field.isAnnotationPresent(RangeFloat.class)) {
                            RangeFloat anno = field.getAnnotation(RangeFloat.class);
                            if (ImGui.sliderFloat(name, imFloat, anno.min(), anno.max())) {
                                field.set(comp, imFloat[0]);
                                variableUpdated = true;
                            }
                        } else {
                            if (ImGui.dragFloat(name, imFloat)) {
                                field.set(comp, imFloat[0]);
                                variableUpdated = true;
                            }
                        }
                    } else if (type == boolean.class) {
                        boolean val = (boolean) value;

                        if (ImGui.checkbox(name, val)) {
                            field.set(comp, !val);
                            variableUpdated = true;
                        }
                    } else if (type == String.class) {
                        String val = (String) value;
                        ImGui.pushID(name);

                        ImString outString = new ImString(val, 256);
                        if (ImGui.inputText(name, outString)) {
                            field.set(comp, outString.get());
                            variableUpdated = true;
                        }
                        ImGui.popID();
                    } else if (type == Vector2f.class) {
                        Vector2f val = (Vector2f) value;

                        if (val == null) val = new Vector2f(0, 0);

                        float[] imVec = {val.x, val.y};
                        if (ImGui.dragFloat2(name, imVec)) {
                            val.set(imVec[0], imVec[1]);
                            variableUpdated = true;
                        }
                        field.set(comp, val);
                    } else if (type == Vector3f.class) {
                        Vector3f val = (Vector3f) value;

                        if (val == null) val = new Vector3f(0, 0, 0);

                        float[] imVec = {val.x, val.y, val.z};
                        if (ImGui.dragFloat3(name, imVec)) {
                            val.set(imVec[0], imVec[1], imVec[2]);
                            variableUpdated = true;
                        }

                        field.set(comp, val);
                    } else if (type == Color.class) {
                        Color val = (Color) value;

                        if (val == null) val = new Color(1.0f, 1.0f, 1.0f);

                        float[] imColor = new float[]{val.red(), val.green(), val.blue(), val.alpha()};
                        if (ImGui.colorEdit4(name, imColor, ImGuiColorEditFlags.AlphaBar)) {
                            val.set(imColor[0], imColor[1], imColor[2], imColor[3]);
                            variableUpdated = true;
                        }

                        field.set(comp, val);
                    } else if (type.isEnum()) {
                        String[] enumValues = EnumUtils.getValues(type);

                        if (value == null && type.getEnumConstants().length > 0) {
                            value = type.getEnumConstants()[0];
                        } else if (type.getEnumConstants().length <= 0) {
                            System.err.println("Cannot have an empty enum, must contain at least one attribute.");
                        }

                        if (value != null) {
                            String enumType = ((Enum) value).name();
                            ImInt index = new ImInt(EnumUtils.getIndex(enumType, enumValues));

                            if (ImGui.combo(field.getName(), index, enumValues, enumValues.length)) {
                                field.set(comp, type.getEnumConstants()[index.get()]);
                                variableUpdated = true;
                            }
                        }
                    }

                    if (ImGui.isItemHovered() && field.isAnnotationPresent(Tooltip.class)) {
                        EditorGUI.tooltip(field.getAnnotation(Tooltip.class).value());
                    }
                }
                ImGui.treePop();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}

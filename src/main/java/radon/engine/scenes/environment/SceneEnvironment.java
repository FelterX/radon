package radon.engine.scenes.environment;

import org.lwjgl.system.MemoryStack;
import radon.engine.graphics.GraphicsFactory;
import radon.engine.graphics.buffers.UniformBuffer;
import radon.engine.lights.DirectionalLight;
import radon.engine.lights.Light;
import radon.engine.lights.PointLight;
import radon.engine.lights.SpotLight;
import radon.engine.resource.Resource;
import radon.engine.scenes.environment.skybox.Skybox;
import radon.engine.util.Color;
import radon.engine.util.IColor;

import java.nio.ByteBuffer;
import java.util.List;

import static java.lang.Math.min;
import static java.util.Objects.requireNonNull;
import static org.lwjgl.system.MemoryStack.stackPush;
import static radon.engine.scenes.environment.SceneLighting.MAX_POINT_LIGHTS;
import static radon.engine.scenes.environment.SceneLighting.MAX_SPOT_LIGHTS;
import static radon.engine.util.Maths.roundUp2;
import static radon.engine.util.types.DataType.INT32_SIZEOF;
import static radon.engine.util.types.DataType.VECTOR4_SIZEOF;

public final class SceneEnvironment implements Resource {

    public static final int DIRECTIONAL_LIGHT_OFFSET = 0;
    public static final int POINT_LIGHTS_OFFSET = Light.SIZEOF;
    public static final int SPOT_LIGHTS_OFFSET = POINT_LIGHTS_OFFSET + Light.SIZEOF * MAX_POINT_LIGHTS;
    public static final int AMBIENT_COLOR_OFFSET = SPOT_LIGHTS_OFFSET + Light.SIZEOF * MAX_SPOT_LIGHTS;
    public static final int FOG_OFFSET = AMBIENT_COLOR_OFFSET + Color.SIZEOF;
    public static final int POINT_LIGHTS_COUNT_OFFSET = roundUp2(FOG_OFFSET + Fog.SIZEOF, VECTOR4_SIZEOF);
    public static final int SPOT_LIGHTS_COUNT_OFFSET = POINT_LIGHTS_COUNT_OFFSET + INT32_SIZEOF;
    public static final int LIGHTS_BUFFER_SIZE = roundUp2(SPOT_LIGHTS_COUNT_OFFSET + INT32_SIZEOF, VECTOR4_SIZEOF);

    public static final Color DEFAULT_AMBIENT_COLOR = new Color(0.8f, 0.8f, 0.8f);
    public static final Color DEFAULT_CLEAR_COLOR = new Color(0.8f, 0.8f, 0.8f);


    private final SceneLighting lights;
    private final Fog fog;
    private final Color ambientColor;
    private final Color clearColor;
    private UniformBuffer lightsBuffer;
    private Skybox skybox;

    public SceneEnvironment() {
        lights = new SceneLighting();
        ambientColor = DEFAULT_AMBIENT_COLOR;
        clearColor = DEFAULT_CLEAR_COLOR;
        fog = new Fog();
        lightsBuffer = GraphicsFactory.get().newUniformBuffer();
        lightsBuffer.allocate(LIGHTS_BUFFER_SIZE);
        lightsBuffer.mapMemory();
    }

    public SceneLighting lighting() {
        return lights;
    }

    public Color ambientColor() {
        return ambientColor;
    }

    public SceneEnvironment ambientColor(IColor ambientColor) {
        this.ambientColor.set(requireNonNull(ambientColor));
        return this;
    }

    public Fog fog() {
        return fog;
    }

    public Color clearColor() {
        return clearColor;
    }

    public SceneEnvironment clearColor(IColor clearColor) {
        this.clearColor.set(requireNonNull(clearColor));
        fog.color(clearColor);
        return this;
    }

    public Skybox skybox() {
        return skybox;
    }

    public SceneEnvironment skybox(Skybox skybox) {
        this.skybox = skybox;
        return this;
    }

    public void update() {

        final DirectionalLight directionalLight = lights.directionalLight();
        final List<PointLight> pointLights = lights.pointLights();
        final List<SpotLight> spotLights = lights.spotLights();

        final int pointLightsCount = min(pointLights.size(), MAX_POINT_LIGHTS);
        final int spotLightsCount = min(spotLights.size(), MAX_SPOT_LIGHTS);

        try (MemoryStack stack = stackPush()) {

            ByteBuffer buffer = stack.calloc(LIGHTS_BUFFER_SIZE);

            if (directionalLight != null) {
                directionalLight.get(DIRECTIONAL_LIGHT_OFFSET, buffer);
            }

            if (pointLightsCount > 0) {
                for (int i = 0; i < pointLightsCount; i++) {
                    pointLights.get(i).get(POINT_LIGHTS_OFFSET + i * Light.SIZEOF, buffer);
                }
            }

            if (spotLightsCount > 0) {
                for (int i = 0; i < spotLightsCount; i++) {
                    spotLights.get(i).get(SPOT_LIGHTS_OFFSET + i * Light.SIZEOF, buffer);
                }
            }

            ambientColor.getRGBA(AMBIENT_COLOR_OFFSET, buffer);
            fog.get(FOG_OFFSET, buffer);
            buffer.putInt(POINT_LIGHTS_COUNT_OFFSET, pointLightsCount);
            buffer.putInt(SPOT_LIGHTS_COUNT_OFFSET, spotLightsCount);

            lightsBuffer.copy(0, buffer);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends UniformBuffer> T buffer() {
        return (T) lightsBuffer;
    }

    @Override
    public void release() {
        lightsBuffer.release();
    }
}

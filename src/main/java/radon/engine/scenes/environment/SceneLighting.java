package radon.engine.scenes.environment;


import radon.engine.lights.DirectionalLight;
import radon.engine.lights.PointLight;
import radon.engine.lights.SpotLight;

import java.util.ArrayList;
import java.util.List;

import static java.lang.StrictMath.min;

public final class SceneLighting {

    public static final int MAX_POINT_LIGHTS = 10;
    public static final int MAX_SPOT_LIGHTS = 10;

    private static final float DEFAULT_SHADOWS_MAX_DISTANCE = 1024;
    private static final int DEFAULT_SHADOW_MAP_SIZE = 2048;


    private DirectionalLight directionalLight;
    private final List<PointLight> pointLights;
    private final List<SpotLight> spotLights;
    private float shadowsMaxDistance;
    private int shadowMapSize;

    SceneLighting() {
        pointLights = new ArrayList<>();
        spotLights = new ArrayList<>();
        shadowsMaxDistance = DEFAULT_SHADOWS_MAX_DISTANCE;
        shadowMapSize = DEFAULT_SHADOW_MAP_SIZE;
    }

    public int lightsCount() {
        int count = directionalLight == null ? 0 : 1;
        count += min(pointLights.size(), 10);
        count += min(spotLights.size(), 10);
        return count;
    }

    public DirectionalLight directionalLight() {
        return directionalLight;
    }

    public SceneLighting directionalLight(DirectionalLight directionalLight) {
        this.directionalLight = directionalLight;
        return this;
    }

    public List<PointLight> pointLights() {
        return pointLights;
    }

    public List<SpotLight> spotLights() {
        return spotLights;
    }

    public float shadowsMaxDistance() {
        return shadowsMaxDistance;
    }

    public SceneLighting shadowsMaxDistance(float shadowsMaxDistance) {
        this.shadowsMaxDistance = shadowsMaxDistance;
        return this;
    }

    public int shadowMapSize() {
        return shadowMapSize;
    }

    public SceneLighting shadowMapSize(int shadowMapSize) {
        this.shadowMapSize = shadowMapSize;
        return this;
    }
}

#version 450 core

//#extension GL_KHR_vulkan_glsl: require

#define MAX_POINT_LIGHTS 10
#define MAX_SPOT_LIGHTS 10

@include "structs/lights.glsl"
@include "structs/fog.glsl"
@include "structs/water_material.glsl"

layout(std140, binding = 0) uniform Camera {
    mat4 projectionViewMatrix;
    vec4 position;
} u_Camera;


layout(std140, binding = 1) uniform Lights {
    Light u_DirectionalLight;
    Light u_PointLights[MAX_POINT_LIGHTS];
    Light u_SpotLights[MAX_SPOT_LIGHTS];
    vec4 u_AmbientColor;
    Fog u_Fog;
    int u_PointLightsCount;
    int u_SpotLightsCount;
};

uniform WaterMaterial u_Material;

uniform sampler2D u_DepthMap;

uniform float u_NearPlane;
uniform float u_FarPlane;

layout(location = 0) in FragmentData {
    vec4 clipSpace;
    vec3 position;
    vec3 normal;
    vec2 textureCoords;
} fragment;


layout(location = 0) out vec4 out_FragmentColor;


const float shininess = 20.0;
const float reflectivity = 0.6;
const float waterFlatFactor = 1.5;
const float edgesBlendFactor = 6.0;
const float distortionBlendFactor = 24.0;

vec3 viewDirection;
vec3 fragmentNormal;


float computeAngle(vec3 v1, vec3 v2) {
    return max(dot(v1, v2), 0.0);
}

float computeAttenuation(vec3 lightPosition, float constant, float linear, float quadratic) {

    float distance = length(lightPosition - fragment.position);

    return 1.0 /
        (constant + linear * distance + quadratic * (distance * distance));
}

float computeIntensity(vec3 normalizedDirection, vec3 lightDirection, float cutOff, float outerCutOff) {

    float theta = dot(normalizedDirection, normalize(lightDirection));

    float epsilon = (cutOff - outerCutOff);

    return clamp((theta - outerCutOff) / epsilon, 0.0, 1.0);
}

vec3 computeSpecularColor(vec3 lightColor, vec3 lightDirection) {

    vec3 reflectDirection = reflect(-lightDirection, fragmentNormal);

    vec3 halfwayDirection = normalize(lightDirection + viewDirection);

    float specular = pow(computeAngle(fragmentNormal, halfwayDirection), shininess);

	return lightColor * specular * reflectivity;
}

vec3 computeDirectionalLight(Light light) {

    vec3 direction = normalize(-light.direction.xyz);

    return computeSpecularColor(light.color.rgb, direction);
}

vec3 computePointLight(Light light) {

    vec3 direction = normalize(light.position.xyz - fragment.position);

    float attenuation = computeAttenuation(light.position.xyz, light.constant, light.linear, light.quadratic);

    return computeSpecularColor(light.color.rgb, direction) * attenuation;
}

vec3 computeSpotLight(Light light) {

    vec3 direction = normalize(light.position.xyz - fragment.position);

    float attenuation = computeAttenuation(light.position.xyz, light.constant, light.linear, light.quadratic);

    float intensity = computeIntensity(direction, light.direction.xyz, light.cutOff, light.outerCutOff);

    return computeSpecularColor(light.color.rgb, direction) * attenuation * intensity;
}

vec4 specularHighlights() {

    vec3 highlights = vec3(0.0);

    if(u_DirectionalLight.type != NULL) {
        highlights += computeDirectionalLight(u_DirectionalLight);
    }

    for(int i = 0;i < u_PointLightsCount;++i) {
        highlights += computePointLight(u_PointLights[i]);
    }

    for(int i = 0;i < u_SpotLightsCount;++i) {
        highlights += computeSpotLight(u_SpotLights[i]);
    }

    return vec4(highlights, 0.0);
}

float linearizeDepth(float depth) {
    float linearDepth = 2.0 * u_NearPlane * u_FarPlane;
    return linearDepth / (u_FarPlane + u_NearPlane - (2.0 * depth - 1.0) * (u_FarPlane - u_NearPlane));
}

float getFloorDistance(vec2 depthCoords) {
    float depth = texture(u_DepthMap, depthCoords).r;
    return linearizeDepth(depth);
}

float getWaterDistance(vec2 depthCoords) {
    return linearizeDepth(gl_FragCoord.z);
}

void main() {

    vec2 ndc = (fragment.clipSpace.xy / fragment.clipSpace.w) / 2.0 + 0.5;

    vec2 reflectionTexCoords = vec2(ndc.x, -ndc.y);
    vec2 refractionTexCoords = ndc;

    float waterDepth = getFloorDistance(ndc) - getWaterDistance(ndc);

    vec2 distortionTexCoords = vec2(0.0);
    vec2 distortion = vec2(0.0);
    
    float textureOffset = u_Material.textureOffset;

    if(testMaterialFlag(u_Material.flags, DUDV_MAP_PRESENT)) {

        vec2 texCoords = fragment.textureCoords;

        distortionTexCoords = texture(u_Material.dudvMap, vec2(texCoords.x + textureOffset, texCoords.y)).rg * 0.1;
        distortionTexCoords = texCoords + vec2(distortionTexCoords.x, distortionTexCoords.y + textureOffset);

        distortion = (texture(u_Material.dudvMap, distortionTexCoords).rg * 2.0 - 1.0) * u_Material.distortionStrength;

        // Do not distort the edges
        float distortionBlending = clamp(waterDepth / distortionBlendFactor, 0.0, 1.0);

        distortion *= distortionBlending;
    }

    if(testMaterialFlag(u_Material.flags, NORMAL_MAP_PRESENT)) {

        fragmentNormal = texture(u_Material.normalMap, distortionTexCoords).rgb;
        fragmentNormal = vec3(fragmentNormal.r * 2.0 - 1.0, fragmentNormal.b * waterFlatFactor, fragmentNormal.g * 2.0 - 1.0);
        fragmentNormal = normalize(fragmentNormal);

    } else {
        fragmentNormal = normalize(fragment.normal);
    }

    reflectionTexCoords += distortion;
    reflectionTexCoords.x = clamp(reflectionTexCoords.x, 0.001, 0.999);
    reflectionTexCoords.y = clamp(reflectionTexCoords.y, -0.999, -0.001);

    refractionTexCoords += distortion;
    refractionTexCoords = clamp(refractionTexCoords, 0.001, 0.999);

    vec4 reflectionColor = texture(u_Material.reflectionMap, reflectionTexCoords);
    vec4 refractionColor = texture(u_Material.refractionMap, refractionTexCoords);

    viewDirection = normalize(u_Camera.position.xyz - fragment.position);
    float reflectionFactor = dot(viewDirection, fragmentNormal);

    bool underWater = u_Camera.position.y - fragment.position.y < 0.0;

    vec4 environmentColor = underWater ? refractionColor : mix(reflectionColor, refractionColor, reflectionFactor);

    vec4 waterColor = mix(environmentColor, u_Material.color, u_Material.colorStrength);

    // Smooth edges by blending them gradually. Blending must be enabled
    waterColor.a = clamp(waterDepth / edgesBlendFactor, 0.0, 1.0);

    // Reduce highlights at the edges so they don't ruin the smooth effect
    float specularEdgeFactor = clamp(waterDepth / edgesBlendFactor, 0.0, 1.0);
    vec4 specularHighlights = specularHighlights() * specularEdgeFactor;

    out_FragmentColor = waterColor + specularHighlights;
}

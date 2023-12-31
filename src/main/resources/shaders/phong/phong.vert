#version 450 core

#extension GL_ARB_bindless_texture : enable

@include "structs/transform.glsl"
@include "structs/shadow_cascade.glsl"

layout(std140, set = 0, binding = 0) uniform Camera {
    mat4 projectionViewMatrix;
    vec4 position;
} u_Camera;

layout(std430, binding = 2) readonly buffer Transforms {
    Transform u_Transforms[];
};

layout(std140, binding = 5) uniform ShadowsInfo {
    ShadowCascade u_ShadowCascades[MAX_SHADOW_CASCADES_COUNT];
    bool u_ShadowsEnabled; 
};

@include "structs/clip_plane.glsl"

uniform vec4 u_ClipPlane;


layout(location = 0) in vec3 in_Position;
layout(location = 1) in vec3 in_Normal;
layout(location = 2) in vec2 in_TexCoords;
layout(location = 3) in int in_TransformIndex;
layout(location = 4) in int in_MaterialIndex;

layout(location = 0) out FragmentData {
    vec3 position;
    vec3 normal;
    vec2 texCoords;
    flat int materialIndex;
    vec4 positionDirLightSpace[MAX_SHADOW_CASCADES_COUNT];
} fragment;


void main() {

    Transform transform = u_Transforms[in_TransformIndex];

    vec4 position = transform.modelMatrix * vec4(in_Position, 1.0);

    gl_ClipDistance[0] = dot(position, u_ClipPlane);

    fragment.position = position.xyz;
    fragment.normal = normalize(mat3(transform.modelMatrix) * in_Normal);
    fragment.texCoords = in_TexCoords;
    fragment.materialIndex = in_MaterialIndex;

    for (int i = 0; i < MAX_SHADOW_CASCADES_COUNT; i++) {
        fragment.positionDirLightSpace[i] = u_ShadowCascades[i].lightMatrix * position;
    }

    gl_Position = u_Camera.projectionViewMatrix * position;
}
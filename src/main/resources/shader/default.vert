#version 330 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec2 texCoord;

out vec2 fragTexCoord;

uniform vec2 offset;
uniform vec2 offsetScale;
uniform int newRandom = 1;

void main() {

    vec3 off = vec3(offset,0);
    vec3 offScale = vec3(offsetScale,0);

    fragTexCoord = texCoord;
    gl_Position = vec4((position * offScale) + off, 1.0);
}

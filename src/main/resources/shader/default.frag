#version 330 core

in vec2 fragTexCoord;
out vec4 fragColor;

uniform sampler2D textureSampler;

uniform vec2 texOffset;
uniform vec2 texScale;
uniform vec4 color = vec4(1,1,1,1);
uniform float noiseLevel = .5;
uniform float seed = 0;
uniform int newRandom = 1;

float oldrandom(vec2 uv)
{
    uv = fract(uv * vec2(123.34, 456.21));
    uv += dot(uv, uv + 45.32);
    return fract(sin(dot(uv, vec2(12.9898, 78.233))) * 43758.5453);
}

float newrand(vec2 co) {
    return fract(sin(dot(co.xy ,vec2(12.9898, 78.233))) * 43758.5453);
}

float random(vec2 uv) {
    if(newRandom == 1) {
        return oldrandom(uv);
    }
    if(newRandom == 0) {
        return newrand(uv);
    }
    return 0;
}


vec4 lerp(vec4 a, vec4 b, float t) {
    return a + (b - a) * t;
}


void main() {
    vec2 textureCoord = (fragTexCoord * texScale) + texOffset;

    vec2 txc = textureCoord;

    vec4 textureColor = texture(textureSampler, textureCoord);

    vec4 recoloredColor = textureColor * color;

    int pixelsX = 200;
    int pixelsY = 200;

    vec2 pixelSeed1 = vec2(floor(textureCoord.x * pixelsX) + seed,floor(textureCoord.y * pixelsY) + seed);
    vec2 pixelSeed2 = vec2(floor(textureCoord.x * pixelsX) + seed,floor(textureCoord.y * pixelsY) + seed);
    vec2 pixelSeed3 = vec2(floor(textureCoord.x * pixelsX) + seed,floor(textureCoord.y * pixelsY) + seed);
    vec2 pixelSeed4 = vec2(floor(textureCoord.x * pixelsX) + seed,floor(textureCoord.y * pixelsY) + seed);

    vec4 randomColor = vec4(random(pixelSeed1),random(pixelSeed2),random(pixelSeed3),random(pixelSeed4)) * lerp(vec4(1), color, 0.4);

    fragColor = lerp(recoloredColor,randomColor,noiseLevel);
}

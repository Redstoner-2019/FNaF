#version 330 core
out vec4 FragColor;

in vec2 vTexCoord;

uniform sampler2D uTexture;
uniform float iTime;
uniform vec2 iResolution;

float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

float noise(vec2 uv) {
    return rand(uv + iTime);
}

void main()
{
    vec2 uv = vTexCoord;

    // Vertical jitter (random but subtle)
    float jitter = step(0.98, fract(sin(iTime * 2.0) * 43758.5453)) * 0.01;
    uv.y += jitter;

    // Wavy distortion
    uv.x += sin(uv.y * 40.0 + iTime * 5.0) * 0.005;

    // Horizontal glitch lines (some static offsets)
    float band = step(0.95, rand(vec2(floor(uv.y * 20.0), iTime))) * 0.03;
    uv.x += band;

    // RGB channel split
    float offset = 0.003 + sin(iTime * 5.0) * 0.001;
    float r = texture(uTexture, uv + vec2(offset, 0.0)).r;
    float g = texture(uTexture, uv).g;
    float b = texture(uTexture, uv - vec2(offset, 0.0)).b;
    vec3 color = vec3(r, g, b);

    // Scanlines
    float scanline = sin(uv.y * iResolution.y * 1.5) * 0.07;
    color -= scanline;

    // Grain (subtle gray-ish noise)
    float grain = (rand(uv * iResolution.xy + iTime * 10.0) - 0.5) * 0.08;
    color += vec3(grain);

    // Occasional mild block corruption
    vec2 blockUv = floor(uv * vec2(160.0, 90.0)) / vec2(160.0, 90.0);
    float blockNoise = rand(blockUv + floor(iTime * 2.0));
    if (blockNoise > 0.97) {
        color *= 0.4 + blockNoise * 0.5; // dark glitch, not colorful
    }

    FragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}

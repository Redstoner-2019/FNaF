#version 330 core

in vec2 fragTexCoord;
out vec4 fragColor;

uniform sampler2D textureSampler;

uniform vec2 texOffset;
uniform vec2 texScale;
uniform vec3 color = vec3(1,1,1);
uniform float noiseLevel = .5;
uniform float seed = 0;



float random(vec2 st)
{
    return ((fract(sin(dot(st.xy, vec2(12.9898,78.233))) * 43758.5453123)));
}

vec4 lerp(vec4 a, vec4 b, float t) {
    return a + (b - a) * t;
}


void main() {
    //vec4 previousColor = texelFetch(prevFramebuffer, coord, 0);

    vec2 textureCoord = (fragTexCoord * texScale) + texOffset;

    vec2 txc = textureCoord;

    vec4 textureColor = texture(textureSampler, textureCoord);

    vec4 recoloredColor = textureColor * vec4(color,0);

    vec4 randomColor = vec4(random(vec2(seed,seed+1) + txc),random(vec2(seed,seed+2) + txc),random(vec2(seed,seed+3) + txc),random(vec2(seed,seed+4) + txc));

    if(textureColor.a == 0) {
        //is transparent pixel
        fragColor = vec4(1,0,1,0);
    } else {
        //is not transparent
        fragColor = lerp(textureColor,randomColor,noiseLevel);
    }

    fragColor = lerp(textureColor,randomColor,noiseLevel);
    //fragColor = vec4(textureColor.xy,textureColor.a,1);
}

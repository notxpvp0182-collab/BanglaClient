#version 330

uniform sampler2D DiffuseSampler;
in vec2 texCoord;
out vec4 fragColor;

layout(std140) uniform PostFxConfig {
    float SharpenAmount;
    float FxaaEnabled;
    float InvWidth;
    float InvHeight;
};

void main() {
    vec2 px = vec2(InvWidth, InvHeight);
    vec3 center = texture(DiffuseSampler, texCoord).rgb;
    vec3 n = texture(DiffuseSampler, texCoord + vec2(0.0, -px.y)).rgb;
    vec3 s = texture(DiffuseSampler, texCoord + vec2(0.0, px.y)).rgb;
    vec3 e = texture(DiffuseSampler, texCoord + vec2(px.x, 0.0)).rgb;
    vec3 w = texture(DiffuseSampler, texCoord + vec2(-px.x, 0.0)).rgb;

    vec3 avg = (n + s + e + w) * 0.25;
    vec3 sharpened = center + (center - avg) * SharpenAmount;
    if (FxaaEnabled > 0.5) {
        sharpened = mix(sharpened, (center * 2.0 + n + s + e + w) / 6.0, 0.35);
    }
    fragColor = vec4(clamp(sharpened, 0.0, 1.0), 1.0);
}

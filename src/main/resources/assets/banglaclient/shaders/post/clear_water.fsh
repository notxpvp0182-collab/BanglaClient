#version 330

uniform sampler2D DiffuseSampler;
in vec2 texCoord;
out vec4 fragColor;

layout(std140) uniform ClearWaterConfig {
    float ClarityStrength;
    float SurfaceTransparency;
    float BrightnessBoost;
    float FogSuppression;
};

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);
    vec3 boosted = color.rgb * (1.0 + BrightnessBoost * 0.22);
    boosted.b = mix(boosted.b, boosted.g * 1.05, ClarityStrength * 0.35);
    fragColor = vec4(clamp(boosted, 0.0, 1.0), color.a);
}

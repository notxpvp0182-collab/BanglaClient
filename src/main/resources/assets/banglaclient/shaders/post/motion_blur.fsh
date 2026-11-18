#version 330

uniform sampler2D DiffuseSampler;
in vec2 texCoord;
out vec4 fragColor;

layout(std140) uniform MotionBlurConfig {
    float BlurStrength;
    float VelocityX;
    float VelocityY;
    float Padding;
};

void main() {
    vec2 dir = vec2(VelocityX, VelocityY) * BlurStrength * 0.012;
    vec4 acc = texture(DiffuseSampler, texCoord) * 0.40;
    acc += texture(DiffuseSampler, clamp(texCoord - dir * 0.5, 0.0, 1.0)) * 0.30;
    acc += texture(DiffuseSampler, clamp(texCoord - dir, 0.0, 1.0)) * 0.20;
    acc += texture(DiffuseSampler, clamp(texCoord - dir * 1.5, 0.0, 1.0)) * 0.10;
    fragColor = vec4(acc.rgb, 1.0);
}

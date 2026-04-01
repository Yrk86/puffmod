#version 120

uniform sampler2D DiffuseSampler;
varying vec2 uv;

void main() {
    vec2 offset = vec2(0.0025);
    vec4 color = vec4(0.0);
    color += texture2D(DiffuseSampler, uv + vec2(-offset.x * 2.0, 0.0)) * 0.15;
    color += texture2D(DiffuseSampler, uv + vec2(-offset.x, 0.0)) * 0.2;
    color += texture2D(DiffuseSampler, uv) * 0.3;
    color += texture2D(DiffuseSampler, uv + vec2(offset.x, 0.0)) * 0.2;
    color += texture2D(DiffuseSampler, uv + vec2(offset.x * 2.0, 0.0)) * 0.15;
    color += texture2D(DiffuseSampler, uv + vec2(-offset.x * 2.0, -offset.y)) * 0.05;
    color += texture2D(DiffuseSampler, uv + vec2(offset.x * 2.0, offset.y)) * 0.05;
    gl_FragColor = color;
}

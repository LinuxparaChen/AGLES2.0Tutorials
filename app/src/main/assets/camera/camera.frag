
#extension GL_OES_EGL_image_external : require//请求使用外部纹理。摄像头纹理需要开启。
precision mediump float;

varying vec2 v_texCoord;
//纹理坐标矩阵，前后摄像头位置旋转角度不一样，返回的图片数据不是正向图片。
//此矩阵可以纠正，由系统返回。
uniform mat4 u_coordMatrix;

uniform samplerExternalOES u_texSampler;//扩展（外部）纹理采样器

void main() {
	gl_FragColor = texture2D(u_texSampler,(u_coordMatrix * vec4(v_texCoord,0.0,1.0)).xy);
}

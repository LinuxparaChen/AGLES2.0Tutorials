attribute vec3 a_position;
attribute vec2 a_texCoord;
uniform vec3 u_lightPos;//光源位置,不能用attribute，如果非要用需要生成与顶点个数相同个颜色。
uniform vec3 u_viewPos;//观察点位置
uniform vec4 u_lightColor;//光源颜色

uniform mat4 u_MMatrix;
uniform mat4 u_VMatrix;
uniform mat4 u_ProjMatrix;

varying vec2 v_texCoord;

varying vec3 v_lightPos;
varying vec3 v_viewPos;
varying vec3 v_fragPos;
varying vec3 v_N;

varying vec4 v_lightColor;
void main() {
	gl_Position = u_ProjMatrix * u_VMatrix * u_MMatrix * vec4(a_position,1.0);
	v_texCoord = a_texCoord;

	v_lightPos = u_lightPos;
	v_viewPos = u_viewPos;
	v_fragPos = (u_MMatrix * vec4(a_position,1.0)).xyz;
//  在这个GLSL版本中没有transpose、inverse函数，我们在u_MMatrix没有做不等比例缩放，这一步可以省略。
//	mat4 nMatrix = transpose(inverse(u_MMatrix));//法向量矩阵
	//法向量
	v_N = normalize((u_MMatrix * vec4(a_position,1.0)).xyz);
	v_lightColor = u_lightColor;
}

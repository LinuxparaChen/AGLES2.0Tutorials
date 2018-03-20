
attribute vec3 a_position;
attribute vec2 a_texCoord;

uniform mat4 u_MMatrix;
uniform mat4 u_VMatrix;
uniform mat4 u_ProjMatrix;

varying vec2 v_texCoord;

void main() {
	gl_Position = u_ProjMatrix * u_VMatrix * u_MMatrix * vec4(a_position, 1.0);
	v_texCoord = a_texCoord;
}

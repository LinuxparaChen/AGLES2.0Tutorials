
//三角形的顶点坐标
attribute vec3 a_position;//attribute 属性修饰符
//三角形的顶点颜色值(rgba)
attribute vec4 a_color;
//模型矩阵
uniform mat4 u_MMatrix;//uniform 统一修饰符
//观察矩阵
uniform mat4 u_VMatrix;
//投影矩阵
uniform mat4 u_ProjMatrix;
//传给片元着色器的颜色值
varying vec4 v_color;//varying 顶点、片元着色器传值，变量名需要完全一致。

void main() {
    //gl_Position 内部变量。
	gl_Position = u_ProjMatrix * u_VMatrix * u_MMatrix * vec4(a_position,1.0);
	v_color = a_color;
}

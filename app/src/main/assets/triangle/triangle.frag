//指定float的精度为中等
precision mediump float;//必须指定，有时候的错误是因为没有指定精度，找错找不到。
//接收顶点着色器传过来的颜色变量
varying vec4 v_color;

void main() {
    //gl_FragColor 内部变量
	gl_FragColor = v_color;
}

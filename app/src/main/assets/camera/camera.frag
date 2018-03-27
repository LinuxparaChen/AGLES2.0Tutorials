
#extension GL_OES_EGL_image_external : require//请求使用外部纹理。摄像头纹理需要开启。
precision mediump float;

varying vec2 v_texCoord;
//纹理坐标矩阵，前后摄像头位置旋转角度不一样，返回的图片数据不是正向图片。
//此矩阵可以纠正，由系统返回。
uniform mat4 u_coordMatrix;

uniform int u_effect;
uniform float u_warmCoolStrength;//冷暖色调强度

uniform int u_kernelSize;//卷积核大小
uniform ivec2 u_imgWH;//图像大小

uniform samplerExternalOES u_texSampler;//扩展（外部）纹理采样器

void main() {
    vec4 texColor = texture2D(u_texSampler,(u_coordMatrix * vec4(v_texCoord,0.0,1.0)).xy);
    if(u_effect == 0){
        //原始
	    gl_FragColor = texColor;
	}else if(u_effect == 1){
	    //灰度
	    float gray = 0.299 * texColor.r + 0.587 * texColor.g + 0.114 * texColor.b;
	    gl_FragColor = vec4(gray,gray,gray,texColor.a);
	}else if(u_effect == 2){
	    //底片
	    gl_FragColor = vec4(1.0 - texColor.r,1.0 - texColor.g,1.0 - texColor.b,texColor.a);
	}else if(u_effect == 3){
	    //冷暖色
	    if(u_warmCoolStrength > 0.0){
	        //暖色
	        gl_FragColor = vec4(texColor.r + u_warmCoolStrength,texColor.g,texColor.b,texColor.a);
	    }else{
	        //冷色
	        gl_FragColor = vec4(texColor.r,texColor.g,texColor.b + u_warmCoolStrength,texColor.a);
	    }
	}else if(u_effect == 4){
	    //浮雕
	    int kernel_r = u_kernelSize/2;//卷积核半径
	    float kernel_r_s = float(kernel_r)/float(u_imgWH.s);//卷积核半径，换算成纹理单位s轴
	    float kernel_r_t = float(kernel_r)/float(u_imgWH.t);//卷积核半径，换算成纹理单位t轴
        vec4 lt_color = texture2D(u_texSampler,(u_coordMatrix * vec4(v_texCoord.s - kernel_r_s,v_texCoord.t + kernel_r_t,0.0,1.0)).xy);
        vec4 rb_color = texture2D(u_texSampler,(u_coordMatrix * vec4(v_texCoord.s + kernel_r_s,v_texCoord.t - kernel_r_t,0.0,1.0)).xy);
        gl_FragColor = rb_color - lt_color + 0.5;
	}else if(u_effect == 5){
	    //雕刻
	    int kernel_r = u_kernelSize/2;//卷积核半径
	    float kernel_r_s = float(kernel_r)/float(u_imgWH.s);//卷积核半径，换算成纹理单位s轴
	    float kernel_r_t = float(kernel_r)/float(u_imgWH.t);//卷积核半径，换算成纹理单位t轴
        vec4 lt_color = texture2D(u_texSampler,(u_coordMatrix * vec4(v_texCoord.s - kernel_r_s,v_texCoord.t + kernel_r_t,0.0,1.0)).xy);
        vec4 rb_color = texture2D(u_texSampler,(u_coordMatrix * vec4(v_texCoord.s + kernel_r_s,v_texCoord.t - kernel_r_t,0.0,1.0)).xy);
        gl_FragColor = lt_color - rb_color + 0.5;
	}
}

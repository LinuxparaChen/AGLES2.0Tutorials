precision mediump float;

varying vec2 v_texCoord;

uniform sampler2D u_texSampler;

varying vec3 v_lightPos;
varying vec3 v_viewPos;
varying vec3 v_fragPos;
varying vec3 v_N;

varying vec4 v_lightColor;

/**
 * 环境光计算公式
 * @param lightColor 光源颜色
 * @param ambientStrength 环境光强度
 */
vec4 ambient(vec4 lightColor,float ambientStrength){
    return ambientStrength * lightColor;
}
/**
 * 漫反射光计算公式
 * @param lightColor 光源颜色
 * @param lightPos 光源位置
 * @param fragPos 顶点坐标(世界坐标)
 * @param N 顶点法线(法线为归一化后的向量)
 */
vec4 diffuse(vec4 lightColor,vec3 lightPos,vec3 fragPos,vec3 N){
    vec3 lightDir = normalize(lightPos - fragPos);//归一化后的光源方向
    float diffuseFactor = max(dot(lightDir,N),0.0);//光照方向与法线夹角的余弦值,过滤掉比0小的值
    return diffuseFactor * lightColor;
}
/**
 * 镜面光计算公式
 * @param lightColor 光源颜色
 * @param lightPos 光源位置
 * @param fragPos 顶点坐标(世界坐标)
 * @param N 顶点法向量(归一化后的)
 * @param viewPos 观察点坐标
 * @param reflectDeg 反射度
 * @param specularStrength 镜面光强度
 */
vec4 specular(vec4 lightColor,vec3 lightPos,vec3 fragPos,vec3 viewPos
            ,vec3 N,float reflectDeg,float specularStrength){
    vec3 lightDir = normalize(lightPos - fragPos);//光源方向
    vec3 reflectDir = reflect(-lightDir,N);//反射光方向
    vec3 viewDir = normalize(viewPos - fragPos);//观察点方向
    float specularFactor = pow(max(dot(reflectDir,viewDir),0.0),reflectDeg);
    return specularStrength * specularFactor * lightColor;
}

void main() {
    vec4 ambientColor = ambient(v_lightColor,0.1);
    vec4 diffuseColor = diffuse(v_lightColor,v_lightPos,v_fragPos,v_N);
    vec4 specularColor = specular(v_lightColor,v_lightPos,v_fragPos,v_viewPos,v_N,8.0,1.0);
	vec4 texColor = texture2D(u_texSampler,v_texCoord);
//	gl_FragColor = texColor;
	gl_FragColor = (ambientColor + diffuseColor + specularColor) * texColor;
}

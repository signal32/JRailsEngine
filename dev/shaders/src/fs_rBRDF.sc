$input v_texcoord0, v_wpos

/*
 * Copyright 2011-2021 Branimir Karadzic. All rights reserved.
 * License: https://github.com/bkaradzic/bgfx#license-bsd-2-clause
 */

#include "../common/common.sh"

//SAMPLER2D(s_texColor, 0);

SAMPLER2D(s_albedo, 0);         // Base colour
SAMPLER2D(s_normal, 1);         // Normal
SAMPLER2D(s_metallic, 2);       //
SAMPLER2D(s_roughness, 3);      //
SAMPLER2D(s_ao, 4);             //
SAMPLER2D(s_depth, 5);          // Position
SAMPLER2D(s_stencil, 6);        // IDs

uniform vec4 lightPositions[4];
uniform vec4 lightColors[4];

//uniform vec3 cameraPos;
const vec3 cameraPos = vec3(1.0);

const float pi = 3.14159265359;

vec3 fresnelSchlick(float cosTheta, vec3 F0)
{
    return F0 + (1.0 - F0) * pow(max(1.0 - cosTheta, 0.0), 5.0);
    // BGFX version: return F0 + (1.0 - F0) * pow(1.0 - cosTheta, 5.0) * _strength;
}

// ----------------------------------------------------------------------------
float DistributionGGX(vec3 N, vec3 H, float roughness)
{
    float a = roughness*roughness;
    float a2 = a*a;
    float NdotH = max(dot(N, H), 0.0);
    float NdotH2 = NdotH*NdotH;

    float nom   = a2;
    float denom = (NdotH2 * (a2 - 1.0) + 1.0);
    denom = pi * denom * denom;

    return nom / denom;
}

// ----------------------------------------------------------------------------
float GeometrySchlickGGX(float NdotV, float roughness)
{
    float r = (roughness + 1.0);
    float k = (r*r) / 8.0;

    float num   = NdotV;
    float denom = NdotV * (1.0 - k) + k;

    return num / denom;
}

// ----------------------------------------------------------------------------
float GeometrySmith(vec3 N, vec3 V, vec3 L, float roughness)
{
    float NdotV = max(dot(N, V), 0.0);
    float NdotL = max(dot(N, L), 0.0);
    float ggx2  = GeometrySchlickGGX(NdotV, roughness);
    float ggx1  = GeometrySchlickGGX(NdotL, roughness);

    return ggx1 * ggx2;
}

// ----------------------------------------------------------------------------
void main()
{
    //vec3 worldPos = texture2D(s_depth, v_texcoord0);
    vec3 worldPos = v_wpos;
    vec3 normal = texture2D(s_normal, v_texcoord0);
    //float albedo = 1.0;//= texture2D(s_albedo, v_texcoord0);
    vec3  albedo = texture2D(s_albedo, v_texcoord0);
    float metallic = texture2D(s_metallic, v_texcoord0);
    float roughness = texture2D(s_roughness, v_texcoord0);
    float ao = texture2D(s_ao, v_texcoord0);

    vec3 N = normalize(normal); //TODO sampler
    vec3 V = normalize(cameraPos - worldPos ); // TODO position sampler

    vec3 F0 = vec3(0.04,0.04,0.04);
    F0 = mix(F0, albedo, metallic);

    vec3 Lo = vec3(0.0,0.0,0.0);
    vec3 test = vec3(0.0,0.0,1.0);
    // Direct lighting
    for(int i = 0; i < 4; ++i)
    {
        // Per light radiance
        vec3 L = normalize(lightPositions[i] - worldPos); //TODO sampler --DONE
        vec3 H = normalize( V + L);

        float distance = length(lightPositions[i] - worldPos); //will length() work? TODO replace vec4 with lightposition[i]
        float attenuation = 1.0 / (distance * distance);
        vec3 radiance = lightColors[i] * attenuation;


        // Cook-torrance brdf
        float NDF = DistributionGGX(N, H, roughness);
        float G = GeometrySmith(N, V, L, roughness); //TODO fix (returns 0)
        vec3 F = fresnelSchlick(max(dot(H, V), 0.0), F0);
        //G = 0.1;
        //test = vec3(F,0.0,0.0);
        //test = F;
        //test = radiance;

        vec3 kS = F;
        vec3 kD = vec3(1.0,1.0,1.0) - kS;
        kD *= 1.0 - metallic;

        //test = kD;

        vec3 numerator = NDF * G * F;
        float denominator = 4.0 * max(dot(N,V),0.0) * max(dot(N,L),0.0);
        vec3 specular = numerator/max(denominator, 0.001);

        //test = numerator;

        // add to outgoing irradiance (Lo)
        float NdotL = clamp(dot(N, L), 0.1,1.0);
        //float NdotL = dot(N, L);
        Lo += (kD * albedo / pi + specular) * radiance * NdotL;
        //test = vec3(Lo);
    }

    // Basic ambient factor
    vec3 ambient = mul(vec3(0.03), albedo);//*ao;
    vec3 colourOut = ambient + Lo;

    // Gamma correction
    colourOut = colourOut / (colourOut + vec3(1.0));
    //colourOut = pow(colourOut, vec3(1.0/2.2));


	gl_FragColor = vec4(colourOut,1.0);
	//gl_FragColor = vec4(albedo,0.0,0.0,1.0);
	//gl_FragColor = lightColors[0];
}



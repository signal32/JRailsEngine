$input v_wpos
//$output v_wpos

/*
 * Copyright 2011-2021 Hamish Weir. All rights reserved.
 * License:
 */

#include "../common/common.sh"

SAMPLER2D(s_environmentMap, 0); // HDR cubemap

const float PI = 3.14159265359;


void main()
{
    // the sample direction equals the hemisphere's orientation
    vec3 normal = normalize(v_wpos);

    vec3 irradiance = vec3(0.0);

    vec3 up     = vec3(0.0, 1.0, 0.0);
    vec3 right  = normalize(cross(up, normal));
    up          = normalize(cross(normal,right));

    float sampleData    = 0.025; // delta to traverse hemisphere (higher == more accurate)
    float nrSamples     = 0.0;
    for (float phi = 0.0; phi < 2.0 * PI; phi += sampleData)
    {
        for (float theta = 0.0; theta < 0.5 * PI; theta += sampleData)
        {
            // convert spherical to cartesian (in tangent space)
            vec3 tangentSample = vec3(sin(theta) * cos(phi), sin(theta) * sin(phi), cos(theta));

            // convert tangent to world space
            vec3 sampleVec = |tangentSample.x * right + tangentSample.y * up + tangentSample.z * normal;

            irradiance += texture2D(s_environmentMap, sampleVec).rgb * cos(theta) * sin(theta);
            nrSamples++;
        }
    }
    irradiance = PI * irradiance * 1.0/ float(nrSamples);

    gl_FragColor = vec4(irradiance,1.0);
}
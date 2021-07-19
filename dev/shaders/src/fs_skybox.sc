$input v_wpos
//$output v_wpos

/*
 * Copyright 2011-2021 Hamish Weir. All rights reserved.
 * License:
 */

#include "../common/common.sh"

//SAMPLER2D(s_environmentMap, 0);
SAMPLERCUBE(s_environmentMap,0);

void main()
{
    vec3 envColor = textureCube(s_environmentMap,v_wpos).rgb;
    envColor = envColor/(envColor + vec3(1.0));
    envColor = pow(envColor,vec3(1.0/2.2));

    gl_FragColor = vec4(envColor,1.0);

}
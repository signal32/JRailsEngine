$input a_position, a_color0, a_texcoord0
$output v_texcoord0, v_wpos

/*
 * Copyright 2011-2021 Branimir Karadzic. All rights reserved.
 * License: https://github.com/bkaradzic/bgfx#license-bsd-2-clause
 */

#include "../common/common.sh"

//uniform vec4 model;

void main()
{
	//gl_Position = mul(u_modelViewProj,mul( u_modelView, vec4(a_position, 1.0) ));
	v_wpos = vec3(mul(u_modelView, vec4(a_position,1.0)));
	v_texcoord0 = a_texcoord0;

	gl_Position = mul(u_modelViewProj, vec4(a_position, 1.0) );
	//gl_Position = mul(u_modelViewProj, mul(u_modelView, mul(model, vec4(a_position,1.0)))))


}

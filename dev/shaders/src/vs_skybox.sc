$input a_position
$output v_wpos

/*
 * Copyright 2011-2021 Hamish Weir. All rights reserved.
 * License:
 */

#include "../common/common.sh"

void main()
{
	v_wpos = a_position;

	mat4 rotView = mat4(mat3(u_view)); // eliminate view translation
	vec4 clipPos = mul(u_proj, mul (rotView, vec4(a_position, 1.0)));
    gl_Position = clipPos;
}
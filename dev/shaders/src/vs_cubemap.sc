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
	gl_Position = mul(u_modelViewProj, vec4(a_position, 1.0));
}
package com.railsdev.rails.core.render.shaders;

import static org.lwjgl.bgfx.BGFX.*;

public class DebugShader extends VertexFragmentShader {

    private static final String VERTEX_SHADER = "vs_rBasicUnlit";
    private static final String FRAGMENT_SHADER = "fs_rBasicUnlit";
    private static final int UNIFORM_COUNT = 1;

    public DebugShader() {
        super(VERTEX_SHADER, FRAGMENT_SHADER, UNIFORM_COUNT);
    }

    @Override
    void setUniforms() {
        uniforms[0] = bgfx_create_uniform("s_texColor",   BGFX_UNIFORM_TYPE_VEC4,1);
    }
}

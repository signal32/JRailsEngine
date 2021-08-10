package com.railsdev.rails.core.render.shaders;

import static org.lwjgl.bgfx.BGFX.BGFX_UNIFORM_TYPE_VEC4;
import static org.lwjgl.bgfx.BGFX.bgfx_create_uniform;

public class CubemapShader extends VertexFragmentShader{

    private static final String VERTEX_SHADER = "vs_cubemap";
    private static final String FRAGMENT_SHADER = "fs_cubemap";
    private static final int UNIFORM_COUNT = 1;

    public CubemapShader() {
        super(VERTEX_SHADER, FRAGMENT_SHADER, UNIFORM_COUNT);
    }

    @Override
    void setUniforms() {
        uniforms[0] = bgfx_create_uniform("s_equirectangularMap",   BGFX_UNIFORM_TYPE_VEC4,1);
    }
}

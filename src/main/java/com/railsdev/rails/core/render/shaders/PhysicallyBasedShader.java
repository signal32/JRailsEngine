package com.railsdev.rails.core.render.shaders;

import static org.lwjgl.bgfx.BGFX.*;

public class PhysicallyBasedShader extends VertexFragmentShader{

    private static final String VERTEX_SHADER = "Vs_rBRDF";
    private static final String FRAGMENT_SHADER = "fs_rBRDF";
    private static final int UNIFORM_COUNT = 5;

    public PhysicallyBasedShader() {
        super(VERTEX_SHADER, FRAGMENT_SHADER, UNIFORM_COUNT);
    }

    @Override
    Shader setUniforms() {

        uniforms[0] = bgfx_create_uniform("s_albedo",   BGFX_UNIFORM_TYPE_VEC4,1);
        uniforms[1] = bgfx_create_uniform("s_normal",   BGFX_UNIFORM_TYPE_VEC4,1);
        uniforms[2] = bgfx_create_uniform("s_metallic", BGFX_UNIFORM_TYPE_VEC4,1);
        uniforms[3] = bgfx_create_uniform("s_roughness",BGFX_UNIFORM_TYPE_VEC4,1);
        uniforms[4] = bgfx_create_uniform("s_ao",       BGFX_UNIFORM_TYPE_VEC4,1);

        return this;
    }
}

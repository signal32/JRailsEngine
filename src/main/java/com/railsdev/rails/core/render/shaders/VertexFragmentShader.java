package com.railsdev.rails.core.render.shaders;

import com.railsdev.rails.core.render.BgfxUtilities;

import java.io.IOException;
import java.io.Serializable;

import static org.lwjgl.bgfx.BGFX.*;

public abstract class VertexFragmentShader implements Shader, Serializable {

    protected static final int MAX_UNIFORMS = 5;

    protected transient short programID;
    protected final String vs;
    protected final String fs;
    protected final short[] uniforms;

    protected VertexFragmentShader(String vs, String fs, int uniformQty){
        this.vs = vs;
        this.fs = fs;
        this.uniforms = new short[uniformQty];
    }

    @Override
    public Shader create() throws IOException {
        short vsID = BgfxUtilities.loadShader(vs);
        short fsID = BgfxUtilities.loadShader(fs);

        programID = bgfx_create_program(vsID,fsID,true);
        return this;
    }

    @Override
    public short getUniform(int stage) {
        return uniforms[stage];
    }

    @Override
    public Shader use(long encoderID) {

        return null;
    }

    @Override
    public short id() {
        return programID;
    }

    @Override
    public void destroy() {

    }

    abstract Shader setUniforms();
}

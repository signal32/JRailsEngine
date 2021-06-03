package com.railsdev.rails.core.render.shaders;

import com.railsdev.rails.core.render.BgfxUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;

import static org.lwjgl.bgfx.BGFX.*;

public abstract class VertexFragmentShader implements Shader, Serializable {

    private static final Logger LOGGER = LogManager.getLogger(VertexFragmentShader.class);

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

        // callback to user to setup the uniforms required.
        setUniforms();

        programID = bgfx_create_program(vsID,fsID,true);

        LOGGER.debug("Created shader ID={} VS={} FS={}",programID,vsID,fsID);

        return this;
    }

    @Override
    public short getUniform(int stage) {
        return uniforms[stage];
    }

    @Override
    public short id() {
        return programID;
    }

    @Override
    public void destroy() {
        onDestroy();
        // Destroy the program - shader handles are already destroyed following program creation.
        bgfx_destroy_program(programID);

        // Destroy all uniforms
        for (var id : uniforms){
            bgfx_destroy_uniform(id);
        }
        LOGGER.debug("Destroyed shader ID={}",programID);
    }

    abstract void setUniforms();
    protected void onDestroy(){}
}

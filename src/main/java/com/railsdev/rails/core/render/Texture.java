package com.railsdev.rails.core.render;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;

import static org.lwjgl.bgfx.BGFX.*;

public class Texture implements Serializable {

    private static final Logger LOGGER = LogManager.getLogger(Texture.class);

    private transient short id; // Texture handle ID
    private final String path;  // Path to texture file
    public final String type;   // Type of texture

    public Texture(String path, String type) {
        this.path = path;
        this.type = type;
    }

    public Texture create() throws IOException {
        try {
            id = BgfxUtilities.loadTexture(path);
        }
        catch (IOException e){
            LOGGER.error("Could not load '{}' : {}",path,e.getLocalizedMessage());
            id = BgfxUtilities.loadTexture("missing.dds");
        }
        return this;
    }

    public short id(){
        return id;
    }
}

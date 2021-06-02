package com.railsdev.rails.core.render;

import java.io.IOException;
import java.io.Serializable;

import static org.lwjgl.bgfx.BGFX.*;

public class Texture implements Serializable {
    private transient short id; // Texture handle ID
    private final String path;  // Path to texture file
    public final String type;   // Type of texture

    public Texture(String path, String type) {
        this.path = path;
        this.type = type;
    }

    public Texture create() throws IOException {
        id = BgfxUtilities.loadTexture(path);
        return this;
    }

    public short id(){
        return id;
    }
}

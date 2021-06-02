package com.railsdev.rails.core.render;

import static org.lwjgl.bgfx.BGFX.*;

public class Encoder {
    public final long id;

    public Encoder() {
        id = bgfx_encoder_begin(false);
    }

    public void clear(){
        bgfx_encoder_discard(id, BGFX_DISCARD_ALL);
    }

    public void end(){
        bgfx_encoder_end(id);
    }

    public void bindTexture(Texture texture, int stage){
        //bgfx_encoder_set_texture(id, stage,te);
    }
}

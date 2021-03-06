package com.railsdev.rails.core.render;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.bgfx.*;
import org.lwjgl.system.*;
import static org.lwjgl.bgfx.BGFX.*;
import static org.lwjgl.system.MemoryStack.*;

public class Renderer {

    public static final int[] RENDER_PASS_ENV_MAP    = {3,4,5,6,7,8};
    public static final int RENDER_PASS_SCENE      = 1;

    public static class Config{
         public long nativeWindowHandle;
         public int width = 800;
         public int height = 600;
         public String type = "vulkan";
    }

    private static final Logger LOGGER = LogManager.getLogger(Renderer.class);

    protected int renderer = BGFX_RENDERER_TYPE_COUNT;
    MemoryStack stack;

    public Renderer(){
        stack = stackPush();
    }

    public void init(Config config){
        BGFXInit init = BGFXInit.mallocStack(stack);
        bgfx_init_ctor(init);
        init.resolution(it -> it.width(config.width).height(config.height).reset(BGFX_RESET_MSAA_X16));
        init.type(BGFX_RENDERER_TYPE_VULKAN);

        init.platformData().nwh(config.nativeWindowHandle);

        if (!bgfx_init(init)){
            throw new RuntimeException("Error initialising bgfx");
        }

        //if (renderer == BGFX_RENDERER_TYPE_COUNT) {
            //renderer = bgfx_get_renderer_type();
        //}
        renderer = bgfx_get_renderer_type();
        BgfxUtilities.configure(renderer);

        LOGGER.info("Renderer started. Using {} subsystem", () -> bgfx_get_renderer_name(renderer));

        bgfx_set_debug(BGFX_DEBUG_TEXT);
        bgfx_set_view_clear(RENDER_PASS_SCENE,BGFX_CLEAR_COLOR | BGFX_CLEAR_DEPTH,0x303030ff, 1.0f,0);
        bgfx_set_view_rect(RENDER_PASS_SCENE,0,0,config.width,config.height);

        bgfx_touch(RENDER_PASS_SCENE);

    }

    public void update(double delta){
        bgfx_dbg_text_clear(0, false);
    }

    public void pushDebugText(int col, int row, CharSequence value){
        bgfx_dbg_text_printf(col,row,0x1f,value);
    }

    public void shutdown(){
        bgfx_shutdown();
    }

}

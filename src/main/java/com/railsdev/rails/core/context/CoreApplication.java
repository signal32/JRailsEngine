package com.railsdev.rails.core.context;

import com.railsdev.rails.core.render.Renderer;
import org.lwjgl.glfw.*;
import org.lwjgl.system.*;

import static org.lwjgl.bgfx.BGFX.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

public abstract class CoreApplication implements Application {

    private static final double FRAMETIME = 1.0/25.0;

    protected long window;
    protected Renderer renderer;

    public CoreApplication() {
    }

    @Override
    public void start(Config appConfig) {

        // start glfw
        if (!glfwInit())
            throw new RuntimeException("Could not initialise GLFW");

        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);

        if (Platform.get() == Platform.MACOSX)
            glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW_FALSE);

        window = glfwCreateWindow(1920,1080,"Test!",0,0);
        if (window == NULL){
            throw new RuntimeException("Could not create window");
        }

        // Register callbacks
        glfwSetKeyCallback(window,(windowHnd, key, scancode, action, mods) -> {
            if (action != GLFW_RELEASE) {
                return;
            }

            switch (key) {
                case GLFW_KEY_ESCAPE:
                    glfwSetWindowShouldClose(windowHnd, true);
                    break;
            }
        });

        // start renderer
        Renderer.Config config = new Renderer.Config();
        config.height = 1080;
        config.width = 1920;
        config.nativeWindowHandle = GLFWNativeWin32.glfwGetWin32Window(window); //TODO need some platform abstraction here

        renderer = new Renderer();
        renderer.init(config);

        beforeStart(this);

        long timeStart = System.currentTimeMillis();
        double accumulator = FRAMETIME;
        double delta = 1.0/60.0;

        // Start loop
        while(!glfwWindowShouldClose(window)){
            glfwPollEvents();

            while(accumulator >= FRAMETIME){
                logicEvent(FRAMETIME);

                accumulator -= FRAMETIME;
            }

            drawEvent(delta);

            long timeEnd = System.currentTimeMillis();
            delta = (timeEnd - timeStart)/1000000.0;
            accumulator += delta;
            timeStart = timeEnd;
        }

        bgfx_shutdown();
        shutdown();

    }

    @Override
    public void shutdown() {
        glfwDestroyWindow(window);
        glfwTerminate();

    }

    @Override
    public void run() {

    }


    abstract public void beforeStart(Application application);
}

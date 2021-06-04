package com.railsdev.rails.core.context;

import com.railsdev.rails.core.render.Renderer;
import com.railsdev.rails.core.utils.Sync;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.lwjgl.bgfx.BGFX.*;
import static org.lwjgl.glfw.GLFW.*;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.nanoTime;

public abstract class CoreApplication implements Application {

    private static final Logger LOGGER = LogManager.getLogger(CoreApplication.class);

    private static final int    TARGET_FRAMERATE    = 5;
    private static final double UNITS               = 100_000_000; //ns
    private static final long   UPDATES_PER_SECOND  = 25;
    private static final double FRAMETIME           = UNITS/UPDATES_PER_SECOND;
    private static final int    MAX_UPDATES         = 10;

    protected long window;
    protected Window mainWindow;
    protected Renderer renderer;
    protected Context context;

    private final Sync sync = new Sync();

    protected CoreApplication() {
        context = new DesktopContext();
        renderer = new Renderer();
    }

    @Override
    public void start(Config appConfig) {

        // Start context subsystem
        context.init();

        Window.Config windowConfig = new Window.Config();
        windowConfig.type = Window.Type.WINDOWED;
        windowConfig.title = appConfig.title;
        windowConfig.width = appConfig.width;
        windowConfig.height = appConfig.height;
        try {
            mainWindow = context.createWindow(windowConfig);
        } catch (Exception e){
            LOGGER.error(e.getMessage());
        }

        // start renderer subsystem
        Renderer.Config config = new Renderer.Config();
        config.height = appConfig.height;
        config.width = appConfig.width;
        config.nativeWindowHandle = mainWindow.handle();
        renderer.init(config);

        // Callback to user defined initialisation (after core subsystems are initialised)
        init(this);

        LOGGER.info("Initialisation completed");


        // Prepare application loop
        long timeStart = System.currentTimeMillis();
        double accumulator = FRAMETIME;
        double delta = 1.0/60.0;

        LOGGER.debug("Starting application loop: frametime={} deta={}",FRAMETIME,delta);

        this.loop();

        // Enter loop
        while(!glfwWindowShouldClose(mainWindow.id())){

            // Get inputs
            context.update();

            while(accumulator >= delta){

                update(delta);
                accumulator -= delta;
            }


            renderer.update(0);
            renderer.pushDebugText(100,1,String.format("time % 7.3f",delta));
            drawStart(delta);

            long timeEnd = System.currentTimeMillis();
            delta = (timeEnd - timeStart)/1000000.0;
            accumulator += delta;
            timeStart = timeEnd;
        }



        shutdown(this);
        destroy();

    }

    @Override
    public void destroy() {
        bgfx_shutdown();
        //glfwDestroyWindow(window);
        context.shutdown();
        glfwTerminate();
    }

    @Override
    public void run() {

    }

    public abstract void shutdown(Application application);

    public abstract void init(Application application);

    private void loop(){
        double previous = nanoTime();
        double lag = 0.0f;

        while (!glfwWindowShouldClose(mainWindow.id())){
            double current = nanoTime();
            double elapsed = current - previous;
            previous = current;
            lag += elapsed;

            // Get input
            context.update();

            // Update simulation until on time (or escape at MAX_UPDATES if unable to catch up)
            int it = 0;
            while (lag >= FRAMETIME && it < MAX_UPDATES) {
                this.update(FRAMETIME/UNITS);
                lag -= FRAMETIME;
                it++;
            }

            // Render frame
            double delta = lag/FRAMETIME;
            renderer.pushDebugText(100,1,String.format("Updates/frame=%d delta=%10.5f ns", it, delta));
            drawStart(delta);
            renderer.update(delta);
            drawEnd(delta);

            sync.sync(TARGET_FRAMERATE);

        }
    }

}

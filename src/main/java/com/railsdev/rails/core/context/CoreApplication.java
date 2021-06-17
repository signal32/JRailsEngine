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

    private static final int    TARGET_FRAMERATE    = 60;
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

        // Enter loop
        this.loop();

        LOGGER.info("Exiting...");

        shutdown(this);
        destroy();
        LOGGER.info("Application terminated");

    }

    @Override
    public void destroy() {
        renderer.shutdown();
        context.shutdown();
        LOGGER.info("Systems shutdown");
    }

    @Override
    public void run() {

    }

    public abstract void shutdown(Application application);

    public abstract void init(Application application);

    private void loop(){
        LOGGER.debug("Starting application main loop: Target update interval = {}/s. Target render interval = {}/s. Frametime ~ {}ns",UPDATES_PER_SECOND,TARGET_FRAMERATE, FRAMETIME);
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

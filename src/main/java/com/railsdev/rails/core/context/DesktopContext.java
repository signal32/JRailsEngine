package com.railsdev.rails.core.context;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.Platform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class DesktopContext implements Context {

    private static final Logger LOGGER = LogManager.getLogger(DesktopContext.class);

    List<Window> windows = new ArrayList<>();

    public DesktopContext() {
    }

    @Override
    public Window createWindow(Window.Config config) throws IOException {
        Window window = new GLFWWindow(config);
        windows.add(window);
        return window;
    }

    @Override
    public boolean init() {

        if (!glfwInit())
            throw new RuntimeException("Could not initialise GLFW");

        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);

        if (Platform.get() == Platform.MACOSX)
            glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW_FALSE);

        PointerBuffer errorDesc = PointerBuffer.allocateDirect(256);
        int errorCode;
        while ((errorCode = glfwGetError(errorDesc)) != GLFW_NO_ERROR){
            LOGGER.warn("GLFW: {} [{}]", errorDesc.getStringUTF8(), errorCode);
        }

        LOGGER.info("Context created");
        return true;
    }

    @Override
    public void update() {
        glfwPollEvents();
    }

    @Override
    public void shutdown() {
        for (Window w : windows){
            glfwDestroyWindow(w.id());
        }
    }
}

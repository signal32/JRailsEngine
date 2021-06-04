package com.railsdev.rails.core.context;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.*;
import org.lwjgl.system.*;

import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

public class GLFWWindow implements Window{

    private static final Logger LOGGER = LogManager.getLogger(GLFWWindow.class);

    protected Config config;
    protected long nwh;
    protected long glfwID;

    protected GLFWWindow(Config config) throws IOException {
        this.config = config;
        this.nwh = -1;

        if (!glfwInit())
            throw new RuntimeException("Could not initialise GLFW");

        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);

        long monitor = (config.type == Type.FULLSCREEN) ? glfwGetPrimaryMonitor() : 0;
        if (config.type == Type.BORDERLESS) glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);

        glfwID = glfwCreateWindow(config.width, config.height, config.title,monitor,0);
        if (glfwID == NULL){
            PointerBuffer errorDesc = PointerBuffer.allocateDirect(256);
            int errorCode = glfwGetError(errorDesc);
            throw new IOException("Could not create window: " + errorCode + " - " + errorDesc.getStringUTF8());
        }

        this.nwh = GLFWNativeWin32.glfwGetWin32Window(glfwID);
        if (nwh == -1){
            PointerBuffer errorDesc = PointerBuffer.allocateDirect(256);
            int errorCode = glfwGetError(errorDesc);
            throw new IOException("Could not get native window handle: " + errorCode + " - " + errorDesc.getStringUTF8());
        }

        setCallbacks();

        LOGGER.debug("Created window '{}': ID: {} NWH:{}", config.title, glfwID, nwh);

    }

    @Override
    public void update(Config config) {
        this.config = config;
        updateWindow();
    }

    @Override
    public void update(String title) {
        this.config.title = title;
        updateWindow();
    }

    @Override
    public Config config() {
        return config;
    }

    @Override
    public long handle() {
        return this.nwh;
    }

    public long id(){
        return glfwID;
    }

    protected void updateWindow() {
        glfwSetWindowSize(glfwID, config.width, config.height);
        glfwSetWindowTitle(glfwID, config.title);
        LOGGER.debug("Updated window '{}': ID: {} NWH:{}", config.title, glfwID, nwh);
    }

    protected void setCallbacks(){

        glfwSetKeyCallback(glfwID,(windowHnd, key, scancode, action, mods) -> {
            if (action != GLFW_RELEASE) { return; }

            switch (key) {
                case GLFW_KEY_ESCAPE:
                    glfwSetWindowShouldClose(windowHnd, true);
                    break;
            }
        });
    }
}

package com.railsdev.rails.core.context;

import java.io.IOException;

public interface Context {
    abstract Window createWindow(Window.Config config) throws IOException;
    abstract boolean init();
    abstract void update();
    abstract void shutdown();
}

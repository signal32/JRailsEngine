package com.railsdev.rails.core.context;

public interface Application {

    public void start(Config config);
    public void shutdown();

    abstract void run();
    abstract void drawEvent(double delta);
    abstract void logicEvent(double delta);

    class Config{
       public int width;
       public int height;
       public String title;
       public String renderer;
    }
}

package com.railsdev.rails.core.context;

public interface Application {

    public void start(Config config);
    public void destroy();

    abstract void drawStart(double delta);
    abstract void drawEnd(double delta);
    abstract void update(double delta);

    class Config{
       public int width;
       public int height;
       public String title;
       public String renderer;
    }
}

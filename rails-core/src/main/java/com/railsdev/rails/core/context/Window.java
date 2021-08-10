package com.railsdev.rails.core.context;

public interface Window {

    public enum Type{
        FULLSCREEN,
        BORDERLESS,
        WINDOWED
    }

    public class Config{
        public int width;
        public int height;
        public String title;
        public Type type;
    }

    abstract void setConfig(Config config);
    void setTitle(String title);
    Config config();

    /**
     * Get a native window handle for this widow
     * @return nwh
     */
    abstract long handle();

    abstract long id();
}

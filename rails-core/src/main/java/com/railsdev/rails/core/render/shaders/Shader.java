package com.railsdev.rails.core.render.shaders;

import java.io.IOException;


public interface Shader {
    abstract Shader create() throws IOException;
    abstract short getUniform(int stage);
    abstract short id();
    abstract void destroy();

}

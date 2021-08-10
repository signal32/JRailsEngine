package com.railsdev.rails.core.render;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TextureCache {
    private final Map<String, Texture> cache;

    public TextureCache() {
        cache = new HashMap<>();
    }

    public Texture getInstance(String path) throws IOException {
        Texture texture = cache.get(path);
        if (texture == null){
            texture = new Texture(path,"").create();
            cache.put(path,texture);
        }
        return texture;
    }
}

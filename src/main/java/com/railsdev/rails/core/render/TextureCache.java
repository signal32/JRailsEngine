package com.railsdev.rails.core.render;

import java.util.HashMap;
import java.util.Map;

public class TextureCache {
    private Map<String, Texture> cache;

    public TextureCache() {
        cache = new HashMap<>();

    }

    public Texture getInstance(String path){
        Texture texture = cache.get(path);
        if (texture == null){
            texture = new Texture(path,"");
            cache.put(path,texture);
        }
        return texture;
    }



}

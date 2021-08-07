package com.railsdev.rails.core.scene;

import com.railsdev.rails.core.render.Model;
import com.railsdev.rails.core.render.shaders.Shader;

/**
 * Scene graph Node with drawing capabilities
 */
public class DrawableNode extends SceneNode implements Drawable{

    protected Model     model;
    protected Shader    shader;

    public DrawableNode(Model geometry, Shader shader) {
        this.model = geometry;
        this.shader = shader;
    }

    @Override
    public void draw(long encoder, int view) {
        model.draw(encoder, view, shader);
    }

    public Model getModel() { return model; }

    public void setModel(Model model) { this.model = model; }

    public Shader getShader() { return shader; }

    public void setShader(Shader shader) { this.shader = shader; }
}

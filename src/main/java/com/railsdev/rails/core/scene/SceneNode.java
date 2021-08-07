package com.railsdev.rails.core.scene;

import org.joml.Matrix4x3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

/**
 * Base Scenegraph Node
 */
public class SceneNode implements Node {

    private static final Matrix4x3f IDENTITY = new Matrix4x3f();

    protected Matrix4x3f        worldTransform;
    protected Matrix4x3f        localTransform;
    protected SceneNode         parent;
    protected List<SceneNode>   children;
    protected boolean           transformChanged;

    public SceneNode() {
        this.localTransform = new Matrix4x3f();
        this.children = new ArrayList<>();
    }

    @Override
    public void update() {

        if (transformChanged){
            updateWorldTransform();

            for (SceneNode child : children) {
                child.transformChanged = true;
                child.update();
            }
        }
        else for (SceneNode child : children) {
            child.update();
        }
    }

    public void updateWorldTransform(){

        worldTransform.set( (parent != null) ? parent.worldTransform : IDENTITY);
        worldTransform.mul(localTransform);
    }

    @Override
    public void destroy() { }

    //TODO Add methods for all applicable transformations

    public void transform(Matrix4x3f localTransform){
        this.localTransform = localTransform;
        this.transformChanged = true;
    }

    public void translate(Vector3fc translation){
        this.localTransform.translate(translation);
        this.transformChanged = true;
    }
}

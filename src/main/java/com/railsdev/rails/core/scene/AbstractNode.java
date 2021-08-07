package com.railsdev.rails.core.scene;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4x3f;

abstract public class AbstractNode<T> {

    private static final Logger LOGGER = LogManager.getLogger(AbstractNode.class);

    private static final Matrix4x3f IDENTITY = new Matrix4x3f();

    protected AbstractNode  parent;
    protected Matrix4x3f    worldTransform;     //to be cached from calculation i think
    protected Matrix4x3f    localTransform;     //relative to parent
    protected boolean       transformChanged;

    public AbstractNode(Matrix4x3f localTransform, AbstractNode parent) {
        this.localTransform = localTransform;
        this.parent = parent;
        this.worldTransform = new Matrix4x3f();
        this.transformChanged = true;
    }

    public void update(){
        if (transformChanged){
             updateWorldTransform();
        }
    }

    protected void updateWorldTransform(){
        worldTransform.set( (parent != null) ? parent.worldTransform : IDENTITY);
        worldTransform.mul(localTransform);
    }
}

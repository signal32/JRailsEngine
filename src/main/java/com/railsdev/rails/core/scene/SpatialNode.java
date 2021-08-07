package com.railsdev.rails.core.scene;

import org.joml.Matrix4x3f;
import org.joml.Vector3f;

public class SpatialNode extends AbstractNode {

    protected Matrix4x3f    localTransform;     //relative to parent
    protected SpatialNode[] children;

    public SpatialNode(Matrix4x3f localTransform, AbstractNode parent) {
        super(parent);
        this.localTransform = localTransform;
    }

    public Vector3f getTranslation(Vector3f dest){
        return this.localTransform.getTranslation(dest);
    }

    @Override
    public AbstractNode[] getChildren() {
        return children;
    }

    @Override
    public Matrix4x3f getTransform(Matrix4x3f dest) {
        return dest.set(localTransform);
    }

    /*
    Idea: spatial node does not update it's own world co-ord, simply tracks whether it's location has changed. (which implies any childs position will also have changed)
    Each frame a walker will loop through all visible nodes and
        1. calculate world transform (using a stack as it walks tree) ["worldTransform.mul(localTransform)"]
            note: if transformChanged = false it can read directly and not re-calculate <- perhaps skip this optimisation for now
        2. submit to render bucket
     */
}

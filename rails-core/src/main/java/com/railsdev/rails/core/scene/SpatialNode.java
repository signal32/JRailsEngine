package com.railsdev.rails.core.scene;

import org.joml.Matrix4x3f;
import org.joml.Vector3f;

public class SpatialNode extends AbstractNode {

    protected Matrix4x3f    localTransform;     //relative to parent

    public SpatialNode(Matrix4x3f localTransform, AbstractNode parent) {
        super(parent, localTransform.getTranslation(new Vector3f()));
        this.localTransform = localTransform;
    }

    @Override
    public Matrix4x3f getLocalTransform(Matrix4x3f dest) {
        return dest.set(localTransform);
    }

    @Override
    public void drawEvent() {

    }

    @Override
    public void updateEvent() {

    }

    @Override
    public AbstractNode[] get(Vector3f location, AbstractNode[] dest) {
        return new AbstractNode[0];
    }

    /*
    Idea: spatial node does not update it's own world co-ord, simply tracks whether it's location has changed. (which implies any childs position will also have changed)
    Each frame a walker will loop through all visible nodes and
        1. calculate world transform (using a stack as it walks tree) ["worldTransform.mul(localTransform)"]
            note: if transformChanged = false it can read directly and not re-calculate <- perhaps skip this optimisation for now
        2. submit to render bucket
     */
}

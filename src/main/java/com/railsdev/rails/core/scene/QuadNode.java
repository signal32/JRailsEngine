package com.railsdev.rails.core.scene;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix3fc;
import org.joml.Matrix4x3f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class QuadNode extends AbstractNode {

    private static final Logger LOGGER = LogManager.getLogger(QuadNode.class);

    private static final int MAX_SPATIALS = 10;
    private static final int MAX_CHILDREN = 4;
    private static final Vector3fc ORIGIN = new Vector3f(0.0f,0.0f,0.0f);
    private static final int MAX_SIZE = 100;

    private QuadNode[]      children;
    private SpatialNode[]   spatials;
    private int             spatialCount;
    private int             size;

    private Vector3f vector3f = new Vector3f(); // Pre-allocated vector for math operations;

    public QuadNode(Matrix4x3f localTransform, AbstractNode parent) {
        super(localTransform, parent);

        children = null;
        spatials = new SpatialNode[MAX_CHILDREN];
        spatialCount = 0;
    }

    public void addSpatial(SpatialNode spatial){

        // If children already exist, use them.
        if (children != null){
            int quad = getQuad(spatial.localTransform.getTranslation(vector3f));
            children[quad].addSpatial(spatial);
        }
        // Otherwise, store in this node
        if (spatialCount < MAX_SPATIALS){
            spatials[++spatialCount] = spatial;
        }
        // Otherwise, break into smaller quads
        else {
            divide();
        }
    }

    private void divide(){

        for (int i = 0; i < children.length; i++) {
            Matrix4x3f t = new Matrix4x3f().translate(size/2,size/2,0.0f);
            children[i] = new QuadNode(t,this);
        }

    }

    private int getQuad(Vector3fc transform){
        float angle = transform.angle(ORIGIN);

        if (angle < 90)
            return 0;
        else if (angle < 180)
            return 1;
        else if (angle < 270)
            return 2;
        else
            return 3;
    }
}



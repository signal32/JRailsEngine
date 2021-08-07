package com.railsdev.rails.core.scene;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4x3f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class QuadNode extends AbstractNode {

    private static final Logger LOGGER = LogManager.getLogger(QuadNode.class);

    private static final int MAX_SPATIALS = 10;
    private static final int MAX_CHILDREN = 4;
    private static final Vector3fc ORIGIN = new Vector3f(0.0f,0.0f,0.0f);
    private static final int MAX_SIZE = 100;

    private QuadNode        NE, SE, SW, NW;
    private boolean         atomic;
    private SpatialNode[]   spatials;
    private int             spatialCount;
    private float             size;

    private Vector3f vector3f = new Vector3f(); //Pre-allocated vector for math operations
    public QuadNode(Matrix4x3f localTransform, AbstractNode parent) {
        super(localTransform, parent);

        spatials = new SpatialNode[MAX_CHILDREN];
        spatialCount = 0;
    }

    public void addSpatial(SpatialNode spatial){

        // If children already exist, use them.
        if (!atomic) {
            addToQuad(spatial);
            return;
        }

        // Otherwise, store in this node
        spatials[++spatialCount] = spatial;

        // And split if needed.
        if (spatialCount >= MAX_SPATIALS){
            split();
        }
    }

    private void split(){

        // Create new sub-nodes
        NE = new QuadNode(new Matrix4x3f().translate(size/2,size/2,0.0f),this);
        SE = new QuadNode(new Matrix4x3f().translate(-size/2,size/2,0.0f),this);
        SW = new QuadNode(new Matrix4x3f().translate(-size/2,-size/2,0.0f),this);
        NW = new QuadNode(new Matrix4x3f().translate(size/2,size/2,0.0f), this);

        // This node is no longer atomic
        atomic = false;

        // Move spatials into sub-nodes
        for (SpatialNode spatial : spatials){
            addToQuad(spatial);
        }
    }

    private QuadNode getQuad(Vector3fc transform){
        float angle = transform.angle(ORIGIN);

        if (angle < 90)
            return NE;
        else if (angle < 180)
            return SE;
        else if (angle < 270)
            return SW;
        else
            return NW;
    }

    private void addToQuad(SpatialNode spatial){
        QuadNode quad = getQuad(spatial.localTransform.getTranslation(vector3f));
        quad.addSpatial(spatial);
    }
}



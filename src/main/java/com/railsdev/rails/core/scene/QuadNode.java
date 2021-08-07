package com.railsdev.rails.core.scene;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4x3f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class QuadNode extends AbstractNode {

    private static final Logger LOGGER = LogManager.getLogger(QuadNode.class);

    private static final int MAX_SPATIALS = 10;
    private static final int MAX_CHILDREN = 4;
    private static final Vector3fc ORIGIN = new Vector3f(0.0f,0.0f,0.0f);
    public static final float MAX_SIZE = 100; //510064472;

    private final SpatialNode[] spatials = new SpatialNode[MAX_SPATIALS];
    private int                 spatialCount;
    private QuadNode            ne, se, sw, nw;
    private boolean             atomic;
    private float               size;

    private final Vector3f vector3f = new Vector3f(); //Pre-allocated vector for math operations
    public QuadNode(Matrix4x3f localTransform,@Nullable AbstractNode parent, float size) {
        super(localTransform, parent);
        spatialCount = 0;
        atomic = true;
        this.size = size;
    }

    public void addSpatial(SpatialNode spatial){

        // If children already exist, use them.
        if (!atomic) {
            addToQuad(spatial);
            return;
        }

        // Otherwise, store in this node
        spatials[spatialCount++] = spatial;

        // And split if needed.
        if (spatialCount >= MAX_SPATIALS){
            split();
        }
    }

    public SpatialNode[] getSpatials(){
        return this.spatials;
    }

    private void split(){

        // Create new sub-nodes
        ne = new QuadNode(new Matrix4x3f().translate(size/2,size/2,0.0f),this, size/4);
        se = new QuadNode(new Matrix4x3f().translate(-size/2,size/2,0.0f),this, size/4);
        sw = new QuadNode(new Matrix4x3f().translate(-size/2,-size/2,0.0f),this, size/4);
        nw = new QuadNode(new Matrix4x3f().translate(size/2,size/2,0.0f), this, size/4);

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
            return ne;
        else if (angle < 180)
            return se;
        else if (angle < 270)
            return sw;
        else
            return nw;
    }

    private void addToQuad(SpatialNode spatial){
        QuadNode quad = getQuad(spatial.localTransform.getTranslation(vector3f));
        quad.addSpatial(spatial);
    }
}



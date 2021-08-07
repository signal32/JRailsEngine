package com.railsdev.rails.core.scene;

import org.jetbrains.annotations.Nullable;
import org.joml.*;

/**
 * Quad-Tree Node implementation to perform spatial partitioning.
 * @author Hamish Weir
 */
public class QuadNode extends AbstractNode {

    private static final int MAX_SPATIALS = 10;
    private static final int MAX_CHILDREN = 4;

    private final QuadNode[]    quadrants           = new QuadNode[MAX_CHILDREN]; //NE = 0, SE = 1, NW = 2, SW = 3
    private final SpatialNode[] spatials            = new SpatialNode[MAX_SPATIALS];
    private int                 spatialCount        = 0;
    private boolean             atomic              = true;
    private float               size;
    private final Vector2fc     transform;
    private int                 depth;

    //Pre-allocated vector for math operations
    private final Vector3f vec3 = new Vector3f();


    protected QuadNode(@Nullable QuadNode parent, Vector2fc localTransform, float size, int depth) {
        super(parent);
        this.transform = localTransform;
        this.size = size;
        this.depth = depth;
    }

    public QuadNode(float size){
        this(null, new Vector2f(), size,0);
    }


    public void addSpatial(SpatialNode spatial){

        spatial.parent = this;

        // Atomic node has no children, add spatial as leaves
        if (atomic)
            spatials[spatialCount++] = spatial;

        // If children already exist, use them.
        else {
            addToQuad(spatial);

            if (spatialCount >= MAX_SPATIALS)
                split();
        }
    }

    public SpatialNode[] getSpatials(){
        return this.spatials;
    }

    private void split(){

        // Create new sub-nodes
        float newSize = size / 4;
        quadrants[0] = new QuadNode(this,new Vector2f(  newSize,  newSize), newSize, depth++);
        quadrants[1] = new QuadNode(this,new Vector2f(  newSize, -newSize), newSize, depth++);
        quadrants[2] = new QuadNode(this,new Vector2f( -newSize,  newSize), newSize, depth++);
        quadrants[3] = new QuadNode(this,new Vector2f( -newSize, -newSize), newSize, depth++);

        // Move spatials into sub-nodes
        for (SpatialNode spatial : spatials){
            addToQuad(spatial);
        }

        // This node is no longer atomic
        atomic = false;
        spatialCount = 0;
    }

    private int getQuad(Vector3f transform){

        if      (transform.x() >= 0 && transform.z() >= 0) return 0;
        else if (transform.x() <= 0 && transform.z() >= 0) return 1;
        else if (transform.x() >= 0 && transform.z() <= 0) return 2;
        else return 3;

    }

    private void addToQuad(SpatialNode spatial){
        int quad = getQuad(spatial.getTranslation(vec3));
        quadrants[quad].addSpatial(spatial);
    }

    @Override
    public AbstractNode[] getChildren() {
        return quadrants;
    }

    @Override
    public Matrix4x3f getTransform(Matrix4x3f dest) {
        return dest.zero().translate(transform.x(), transform.y(), 0.0f);
    }
}
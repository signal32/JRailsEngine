package com.railsdev.rails.core.scene;

import org.jetbrains.annotations.Nullable;
import org.joml.*;

/**
 * Quad-Tree Node implementation to perform spatial partitioning.
 * @author Hamish Weir
 */
public class QuadNode extends AbstractNode {

    public enum Quad{
        NE,
        SE,
        SW,
        NW
    }

    private static final int MAX_DEPTH          = 10;   // Equates to ~1m square at deepest lever
    private static final int DEFAULT_SIZE       = 1024; // ~ 1km
    private static final int QUADRANT_COUNT     = 4;
    private static final int MAX_CHILDREN       = 2000;
    private static final int SPLIT_THRESHOLD    = 10;



    private final QuadNode[]    quadrants           = new QuadNode[QUADRANT_COUNT]; //NE = 0, SE = 1, NW = 2, SW = 3
    private boolean             atomic              = true;
    private final Vector2fc     bl = new Vector2f(), tr = new Vector2f(); //Top Right & Top Left co-ord
    private int                 depth;
    private float               size;

    //Pre-allocated vector for math operations
    private final Vector3f vec3 = new Vector3f();


    protected QuadNode(@Nullable QuadNode parent, Vector3fc localTranslate, float size, int depth) {
        super(parent, localTranslate);
        this.size = size;
        this.depth = depth;
        System.out.println(size);
        System.out.println(depth);
        System.out.println("----");
    }

    public QuadNode(float size){
        this(null, new Vector3f(), size,0);
    }

    public QuadNode(){
        this(DEFAULT_SIZE);
    }

    protected void split(){

        if (depth >= MAX_DEPTH) return;

        else if (atomic) {
            // Create new sub-nodes
            float newSize = size / 2;
            int newDepth = depth+1;
            quadrants[0] = new QuadNode(this, new Vector3f( newSize, 0.0f,  newSize), newSize, newDepth);
            quadrants[1] = new QuadNode(this, new Vector3f( newSize, 0.0f, -newSize), newSize, newDepth);
            quadrants[2] = new QuadNode(this, new Vector3f(-newSize, 0.0f,  newSize), newSize, newDepth);
            quadrants[3] = new QuadNode(this, new Vector3f(-newSize, 0.0f, -newSize), newSize, newDepth);
        }

        // Move spatials into sub-nodes
 //       for (AbstractNode child : children){
 //           int quad = getQuad(child.getLocalTranslation(vec3));
 //           quadrants[quad].push(child);
 //       }

        for (int i = 0; i < children.size(); i++) {
            AbstractNode child = children.pop();
            int quad = getQuad(child.getLocalTranslation(vec3));
            quadrants[quad].push(child);
        }

        // This node is no longer atomic
        atomic = false;
    }

    private int getQuad(Vector3f transform){

        if      (transform.x >= 0 && transform.z >= 0) return 0;
        else if (transform.x <= 0 && transform.z >= 0) return 1;
        else if (transform.x >= 0 && transform.z <= 0) return 2;
        else return 3;

    }

    private boolean insideQuad(Vector3f transform){
        return transform.x > size / 2 || transform.z > size / 2;
    }

    public void adoptQuadrant(QuadNode quad, Quad pos){
        if (atomic) split();
    }

    @Override
    public boolean push(AbstractNode node) {

        node.parent = this;

        // Check if spatial belongs in this quad or outside
//        if (!insideQuad(node.getLocalTranslation(vec3))){
//            return false;
//        }

        // Atomic node has no children, add spatial as leaves
        if (children.size() < MAX_CHILDREN)
            children.push(node);

        if (children.size() >= SPLIT_THRESHOLD){
            split();
        }

        return true;
    }

    @Override
    public Matrix4x3f getLocalTransform(Matrix4x3f dest) {
        return dest.zero().translate(localTranslation);
    }

    @Override
    public void drawEvent() {

    }

    @Override
    public void updateEvent() {

    }

    @Override
    public AbstractNode[] get(Vector3f location, AbstractNode[] dest) {
        if (atomic) return this.getChildren(dest);
        else{
            return quadrants[getQuad(location)].get(location,dest);
        }
    }

    /**
     * Create a new QuadNode that encompasses a child.
     * @param child Child Node
     * @param pos Desired position of child within parent
     * @return New non-atomic QuadNode containing child
     * @// TODO: 08/08/2021 Improve this shit storm 
     */
    public static QuadNode createParent(QuadNode child, Quad pos){

        QuadNode parent = new QuadNode(null, new Vector3f(), child.size * 2, child.depth - 1);
        parent.split();

        switch (pos){
            case NE: parent.quadrants[0] = child; break;
            case SE: parent.quadrants[1] = child; break;
            case SW: parent.quadrants[2] = child; break;
            case NW: parent.quadrants[3] = child; break;
        }

        child.parent = parent;

        return parent;
    }

    public static void main(String[] args) {
        QuadNode node = new QuadNode();
        for (int i = 0; i < 2600; i++) {
            node.push(new SpatialNode(new Matrix4x3f().translate(1,1,1),null));
        }

    }
}


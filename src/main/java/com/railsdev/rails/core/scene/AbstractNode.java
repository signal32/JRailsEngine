package com.railsdev.rails.core.scene;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4x3f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Stack;

/**
 * Base Node that provides API for iterating spacial partitioning trees.
 * @author Hamish Weir
 * 
 * @// TODO: 08/08/2021 Add ability to remove specific Node from structure
 */
public abstract class AbstractNode {

    private static final int MAX_CHILDREN = 200;

    protected AbstractNode  parent;
    protected Stack<AbstractNode> children;
    protected boolean       transformChanged;
    protected final Vector3fc localTranslation;


    private AbstractNode(@Nullable AbstractNode parent, Vector3fc localTranslation, Stack<AbstractNode> children, int childCount) {
        this.parent = parent;
        this.children = children;
        this.localTranslation = localTranslation;
        this.transformChanged = true;
    }

    protected AbstractNode(@Nullable AbstractNode parent, Vector3fc localTranslation){
        this(parent, localTranslation, new Stack<>(), 0);
    }

    /**
     * @return Direct parent of this node
     */
    public AbstractNode getParent(){
        return this.parent;
    }

    public Vector3f getLocalTranslation(Vector3f dest){
        return dest.set(localTranslation);
    }

    public abstract boolean push(AbstractNode node);
    
    public AbstractNode pop(){
        return children.pop();
    }

    /**
     * @return Direct descendants of this node
     * //TODO this is janky, perhaps replace with a custom itterator
     */
    public AbstractNode[] getChildren(AbstractNode[] dest) {
        return children.toArray(dest);
    }

    /**
     * Get the position of this node in 3D space.
     * @implNote position should be relative to parent (i.e. local transform)
     * @param dest matrix to hold the result
     * @return matrix with result
     */
    public abstract Matrix4x3f getLocalTransform(Matrix4x3f dest);

    /**
     * Called during rendering cycle, allows node to implement render logic
     * Note: only called if this node is deemed to be within visible scene,
     * see {@link #updateEvent()} for guaranteed update.
     */
    public abstract void drawEvent();

    /**
     * Called during each update cycle. Unlike {@link #drawEvent()} this should
     * not be influenced by any culling mechanism.
     */
    public abstract void updateEvent();

    public abstract AbstractNode[] get(Vector3f location, AbstractNode[] dest);

}

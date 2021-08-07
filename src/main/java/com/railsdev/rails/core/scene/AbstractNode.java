package com.railsdev.rails.core.scene;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4x3f;

/**
 * Base Node that provides API for iterating spacial partitioning trees.
 * @author Hamish Weir
 */
public abstract class AbstractNode {

    protected AbstractNode  parent;
    protected boolean       transformChanged;


    protected AbstractNode(@Nullable AbstractNode parent) {
        this.parent = parent;
        this.transformChanged = true;
    }

    /**
     * @return Direct parent of this node
     */
    public AbstractNode getParent(){
        return this.parent;
    }

    /**
     * @return Direct descendants of this node
     */
    public abstract AbstractNode[] getChildren();

    /**
     * Get the position of this node in 3D space.
     * @implNote position should be relative to parent (i.e. local transform)
     * @param dest matrix to hold the result
     * @return matrix with result
     */
    public abstract Matrix4x3f getTransform(Matrix4x3f dest);

}

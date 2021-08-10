package com.railsdev.rails.core.scene;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4x3f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Iterator;
import java.util.Stack;
import java.util.function.Consumer;

/**
 * Base Node that provides API for iterating spacial partitioning trees.
 * @author Hamish Weir
 * 
 * @// TODO: 08/08/2021 Add ability to remove specific Node from structure
 */
public abstract class AbstractNode implements Iterable<AbstractNode> {

    private static final int MAX_CHILDREN = 200;
    private static int COUNTER = 0;

    protected AbstractNode  parent;
    protected Stack<AbstractNode> children;
    protected boolean       transformChanged;
    protected final Vector3fc localTranslation;
    private String name = "AbstractNode_" + AbstractNode.COUNTER;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private AbstractNode(@Nullable AbstractNode parent, Vector3fc localTranslation, Stack<AbstractNode> children, int childCount) {
        this.parent = parent;
        this.children = children;
        this.localTranslation = localTranslation;
        this.transformChanged = true;
        COUNTER++;
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

    public AbstractNode pop(){
        return children.pop();
    }

    public void push(AbstractNode node){
        children.push(node);
    }

    /**
     * @return Direct descendants of this node
     * //TODO this is janky, perhaps replace with a custom itterator
     */
    public AbstractNode[] getChildren(AbstractNode[] dest) {
        return children.toArray(dest);
    }

    @NotNull
    @Override
    public Iterator<AbstractNode> iterator() {
        return new NodeIterator(this);
    }

    @Override
    public void forEach(Consumer<? super AbstractNode> action) {
        children.forEach(action);
    }

    public Matrix4x3f getLocalTransform(Matrix4x3f dest) {
        return dest.zero().translate(localTranslation);
    }

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

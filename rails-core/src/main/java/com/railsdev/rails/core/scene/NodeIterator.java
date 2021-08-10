package com.railsdev.rails.core.scene;

import java.util.Iterator;

public class NodeIterator implements Iterator<AbstractNode> {

    AbstractNode node;
    int position;

    public NodeIterator(AbstractNode node) {
        this.node = node;
        this.position = 0;
    }

    @Override
    public boolean hasNext() {
        return position < node.children.size();
    }

    @Override
    public AbstractNode next() {
        return node.children.get(position++);
    }
}

package com.railsdev.rails.core.scene;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4x3f;

public class SpatialNode extends AbstractNode {

    private static final Logger LOGGER = LogManager.getLogger(SpatialNode.class);

    public SpatialNode(Matrix4x3f localTransform, AbstractNode parent) {
        super(localTransform, parent);
    }
}

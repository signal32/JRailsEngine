package com.railsdev.rails.core.scene.test;

import com.railsdev.rails.core.scene.QuadNode;
import com.railsdev.rails.core.scene.SpatialNode;
import org.joml.Matrix4x3f;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QuadNodeTest {

    private final Matrix4x3f matrix4x3f = new Matrix4x3f();

    //private static getSpatial()


    @Test()
    @DisplayName("Initialisation")
    public void CreateEmptyQuadNode(){
        QuadNode quadNode = new QuadNode(matrix4x3f, null, 100);

        assertNull(quadNode.getSpatials()[0]);
    }

    @Test()
    public void addSpatial(){
        QuadNode quadNode = new QuadNode(matrix4x3f, null, 100);
        quadNode.addSpatial(new SpatialNode(matrix4x3f,quadNode));
        quadNode.addSpatial(new SpatialNode(matrix4x3f,quadNode));
        quadNode.addSpatial(new SpatialNode(matrix4x3f,quadNode));
        quadNode.addSpatial(new SpatialNode(matrix4x3f,quadNode));
        quadNode.addSpatial(new SpatialNode(matrix4x3f,quadNode));
        quadNode.addSpatial(new SpatialNode(matrix4x3f,quadNode));
        quadNode.addSpatial(new SpatialNode(matrix4x3f,quadNode));
        quadNode.addSpatial(new SpatialNode(matrix4x3f,quadNode));
        quadNode.addSpatial(new SpatialNode(matrix4x3f,quadNode));
        quadNode.addSpatial(new SpatialNode(matrix4x3f,quadNode));

        assertTrue(true);
    }

}

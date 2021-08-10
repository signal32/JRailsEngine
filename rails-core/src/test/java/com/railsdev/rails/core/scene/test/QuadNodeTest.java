package com.railsdev.rails.core.scene.test;

import com.railsdev.rails.core.scene.AbstractNode;
import com.railsdev.rails.core.scene.QuadNode;
import com.railsdev.rails.core.scene.SpatialNode;
import org.joml.Matrix4x3f;
import org.joml.Random;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuadNodeTest {

    private final Matrix4x3f matrix4x3f = new Matrix4x3f();

    private float randomFloat(float min, float max){
        Random r = new Random();
        return min + r.nextFloat() * (max - min);
    }

    //private static getSpatial()


    @Test()
    @DisplayName("Initialisation")
    void CreateEmptyQuadNode(){
        QuadNode quadNode = new QuadNode(100);

        //assertNull(quadNode.getChildren()[0]);
    }

    @Test()
    @DisplayName("Add 500 nodes")
    void addSpatial(){
        QuadNode quadNode = new QuadNode(100);

        for (int i = 0; i < 500; i++) {
            quadNode.push(new SpatialNode(new Matrix4x3f().translate(1,1,1),null));
        }

        var stuff = quadNode.get(new Vector3f(0,1,1), new AbstractNode[50]);

        var itr = new QuadNode.QuadTreeIterator(quadNode, new Vector3f(1,1,1), QuadNode.QuadTreeIterator.Strategy.ALL);

        while (itr.hasNext()){
            System.out.println(itr.next());
        }

        assertTrue(true);
    }

    @Test
    @DisplayName("Create Parent")
    void createParent(){
        String childName = "test_child_node";
        int childSize = 1024;

        // Test placing a child in each possible quadrant of parent
        for (QuadNode.Quad quad : QuadNode.Quad.values()){
            QuadNode child = new QuadNode(childSize);
            child.setName(childName);
            QuadNode parent = QuadNode.createParent(child, quad);

            // Check child - parent relationship
            assertEquals(childName, parent.getQuadrant(quad).getName());
            assertEquals(parent.getDepth(), child.getDepth() - 1);
            assertEquals(parent.getSize(), childSize * 2);
            assertFalse(parent.isAtomic());

            // Check child is not also within another quadrant
            for (QuadNode.Quad otherQuad : QuadNode.Quad.values()) {
                if (otherQuad == quad) continue;
                assertNotEquals(childName, parent.getQuadrant(otherQuad).getName());
            }
        }
    }

}

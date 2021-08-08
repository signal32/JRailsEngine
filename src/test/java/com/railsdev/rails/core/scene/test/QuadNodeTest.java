package com.railsdev.rails.core.scene.test;

import com.railsdev.rails.core.scene.AbstractNode;
import com.railsdev.rails.core.scene.QuadNode;
import com.railsdev.rails.core.scene.SpatialNode;
import org.joml.Matrix4x3f;
import org.joml.Random;
import org.joml.Vector3f;
import org.junit.jupiter.api.Assertions;
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
    @DisplayName("Add random spatials stress test")
    void addSpatial(){
        QuadNode quadNode = new QuadNode(100);

        for (int i = 0; i < 30; i++) {
            quadNode.push(new SpatialNode(new Matrix4x3f().translate(1,1,1),null));
        }

        var stuff = quadNode.get(new Vector3f(0,1,1), new AbstractNode[50]);
        //QuadNode newOne = QuadNode.createParent(quadNode, QuadNode.Quad.NE);

        assertTrue(true);
    }

}

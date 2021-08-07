package com.railsdev.rails.core.scene.test;

import com.railsdev.rails.core.scene.QuadNode;
import com.railsdev.rails.core.scene.SpatialNode;
import org.joml.Matrix4x3f;
import org.joml.Random;
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

        assertNull(quadNode.getSpatials()[0]);
    }

    @Test()
    @DisplayName("Add random spatials stress test")
    void addSpatial(){
        QuadNode quadNode = new QuadNode(100);

        for (int i = 0; i < 25; i++) {
            quadNode.addSpatial(new SpatialNode(new Matrix4x3f().translate(randomFloat(-100,100),randomFloat(-100,100),randomFloat(-100,100)),quadNode));
        }

        assertTrue(true);
    }

}

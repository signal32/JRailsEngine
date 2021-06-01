package com.railsdev.rails.core.render.debug;

import com.railsdev.rails.core.render.Mesh;

/**
 * A debug cube Mesh with random vertex colours.
 */
public class DebugCubeTex extends Mesh {

    private static final Object[][] cubeVertices = {
            { -1.0f, 1.0f, 1.0f,    0xff000000,     0.0f, 1.0f },   //TR
            { 1.0f, 1.0f, 1.0f,     0xff0000ff,     1.0f, 1.0f },   //BR
            { -1.0f, -1.0f, 1.0f,   0xff00ff00,     0.0f, 0.0f },   //BL
            { 1.0f, -1.0f, 1.0f,    0xff00ffff,     1.0f, 0.0f },   //TL
            { -1.0f, 1.0f, -1.0f,   0xffff0000,     0.0f, 1.0f },   //Repeat...
            { 1.0f, 1.0f, -1.0f,    0xffff00ff,     1.0f, 1.0f },
            { -1.0f, -1.0f, -1.0f,  0xffffff00,     0.0f, 0.0f },
            { 1.0f, -1.0f, -1.0f,   0xffffffff,     1.0f, 0.0f }
    };

    private static final int[] cubeIndices = {
            0, 1, 2, // 0
            1, 3, 2,
            4, 6, 5, // 2
            5, 6, 7,
            0, 2, 4, // 4
            4, 2, 6,
            1, 5, 3, // 6
            5, 7, 3,
            0, 4, 1, // 8
            4, 5, 1,
            2, 3, 6, // 10
            6, 3, 7
    };
    public DebugCubeTex() {
        super(cubeVertices,cubeIndices);
        setVertexLayout(VertexType.POSITION, VertexType.COLOR, VertexType.UV);
    }
}

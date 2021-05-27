package com.railsdev.rails.core.render;

import org.lwjgl.bgfx.BGFXVertexLayout;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static org.lwjgl.bgfx.BGFX.*;

import static org.lwjgl.bgfx.BGFX.bgfx_vertex_layout_begin;

public class Mesh {

    public enum VertexType{
        POSITION,
        NORMAL,
        COLOR,
        UV,
        TEX1,
        TEX2,
        TEX3,
        TEX4,
    }

    private Object[][] vertices;
    private int[] indices;
    private Texture[] textures;
    private ByteBuffer verticesBuffer;
    private ByteBuffer indicesBuffer;

    private VertexType[] vertexLayout;
    BGFXVertexLayout layout;
    public short vbh;
    public short ibh;


    public Mesh(Object[][] vertices, int[] indices, Texture... textures) {
        this.vertices = vertices;
        this.indices = indices;
        this.textures = textures;
    }

    public Mesh setVertexLayout(VertexType... vertexLayout){
        this.vertexLayout = vertexLayout;
        layout = BGFXVertexLayout.calloc();
        int renderer = bgfx_get_renderer_type();
        bgfx_vertex_layout_begin(layout,renderer);

        for (var vertex : vertexLayout){

            switch (vertex){
                case POSITION:
                    bgfx_vertex_layout_add(layout,BGFX_ATTRIB_POSITION,3,BGFX_ATTRIB_TYPE_FLOAT,false,false);
                    break;
                case NORMAL:
                    bgfx_vertex_layout_add(layout,BGFX_ATTRIB_NORMAL,3,BGFX_ATTRIB_TYPE_FLOAT,false,false);
                    break;
                case UV:
                    bgfx_vertex_layout_add(layout,BGFX_ATTRIB_TEXCOORD0,2,BGFX_ATTRIB_TYPE_FLOAT,false,false);
                    break;
                case COLOR:
                    bgfx_vertex_layout_add(layout,BGFX_ATTRIB_COLOR0,4,BGFX_ATTRIB_TYPE_UINT8,true,false);
                    break;
                default:
                    System.out.println("Skipping unsupported vertex attribute: " + vertex.toString());
            }
        }
        bgfx_vertex_layout_end(layout);
        return this;
    }

    public Mesh create(){
        verticesBuffer = MemoryUtil.memAlloc(vertices.length * (vertices[0].length * 4)); // Crappy - Each vertex attribute element is allocated 4 bytes, even if it is smaller (or bigger)
        vbh = BgfxUtilities.createVertexBuffer(verticesBuffer, layout, vertices);

        indicesBuffer = MemoryUtil.memAlloc(indices.length * 2); // each indices is 2 bytes (short/char)
        ibh = BgfxUtilities.createIndexBuffer(indicesBuffer, indices);

        //Create texture in TextureCache, and get it's id (or just get it's id if created already)

        return this;
    }
}

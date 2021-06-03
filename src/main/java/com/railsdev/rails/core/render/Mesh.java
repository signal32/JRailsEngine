package com.railsdev.rails.core.render;

import com.railsdev.rails.core.render.shaders.Shader;
import org.lwjgl.bgfx.BGFXVertexLayout;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

import static org.lwjgl.bgfx.BGFX.*;

import static org.lwjgl.bgfx.BGFX.bgfx_vertex_layout_begin;

public class Mesh implements Serializable {

    public static final TextureCache textureCache = new TextureCache();

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

    private final Serializable[][] vertices;
    private final int[] indices;
    private final String[] texturePaths;

    private transient ByteBuffer verticesBuffer;
    private transient ByteBuffer indicesBuffer;

    private VertexType[] vertexLayout;
    private BGFXVertexLayout layout;

    private transient short vbh;
    private transient short ibh;
    private transient Texture[] textures;


    /**
     * Create a new Mesh. Does not perform initialisation, use .setVertexLayout() and .create().
     * @param vertices Mesh vertices
     * @param indices Triangle indices
     * @param texturePaths List of textures used by mesh. Order matters, and are mapped 1:1 with texture uniforms of shader when drawn.
     */
    public Mesh(Serializable[][] vertices, int[] indices, String... texturePaths) {
        this.vertices = vertices;
        this.indices = indices;
        this.texturePaths = texturePaths;
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

    public Mesh create() throws IOException {
        verticesBuffer = MemoryUtil.memAlloc(vertices.length * (vertices[0].length * 4)); // Crappy - Each vertex attribute element is allocated 4 bytes, even if it is smaller (or bigger)
        vbh = BgfxUtilities.createVertexBuffer(verticesBuffer, layout, vertices);

        indicesBuffer = MemoryUtil.memAlloc(indices.length * 2); // each indices is 2 bytes (short/char)
        ibh = BgfxUtilities.createIndexBuffer(indicesBuffer, indices);

        //Create texture in TextureCache, and get it's id (or just get it's id if created already)
        textures = new Texture[texturePaths.length];
        for (int i = 0; i < textures.length; i++){
            textures[i] = textureCache.getInstance(texturePaths[i]);
        }
        return this;
    }

    public void draw(long encoder, Shader shader){ // encoder already has some information from higher level (camera position, view matrix etc)
        // set vertex and index buffers
        bgfx_encoder_set_vertex_buffer(encoder,0,vbh,0,vertices.length);
        bgfx_encoder_set_index_buffer(encoder,ibh,0,indices.length);

        // set textures
        for (int i = 0; i < textures.length; i++){
            short sampler  = shader.getUniform(i);
            short handle = textures[i].id();
            bgfx_encoder_set_texture(encoder,i,sampler,handle, 0xffffffff);
        }

        // Submit with shader
        bgfx_encoder_set_state(encoder, BGFX_STATE_DEFAULT | BGFX_STATE_CULL_CCW, 0);
        bgfx_encoder_submit(encoder, 0, shader.id(), 0, 0);

        //bgfx_encoder_discard(encoder, BGFX_DISCARD_VERTEX_STREAMS | BGFX_DISCARD_INDEX_BUFFER | BGFX_DISCARD_BINDINGS);
    }
}

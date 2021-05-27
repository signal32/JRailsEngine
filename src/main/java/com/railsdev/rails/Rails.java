package com.railsdev.rails;

import com.railsdev.rails.core.context.Application;
import com.railsdev.rails.core.context.CoreApplication;
import com.railsdev.rails.core.render.*;
import org.joml.Matrix4f;
import org.joml.Matrix4x3f;
import org.joml.Vector3f;
import org.lwjgl.bgfx.BGFXReleaseFunctionCallback;
import org.lwjgl.bgfx.BGFXVertexLayout;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.bgfx.BGFX.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.nmemFree;


public class Rails extends CoreApplication{

    //begin testing data
    private static final Object[][] cubeVertices = {
            { -1.0f, 1.0f, 1.0f, 0xff000000 },
            { 1.0f, 1.0f, 1.0f, 0xff0000ff },
            { -1.0f, -1.0f, 1.0f, 0xff00ff00 },
            { 1.0f, -1.0f, 1.0f, 0xff00ffff },
            { -1.0f, 1.0f, -1.0f, 0xffff0000 },
            { 1.0f, 1.0f, -1.0f, 0xffff00ff },
            { -1.0f, -1.0f, -1.0f, 0xffffff00 },
            { 1.0f, -1.0f, -1.0f, 0xffffffff }
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

    private BGFXVertexLayout layout;
    private ByteBuffer vertices;
    private short vbh;
    private ByteBuffer indices;
    private short ibh;
    private short program;

    private Matrix4x3f view = new Matrix4x3f();
    private FloatBuffer viewBuf;
    private Matrix4f proj = new Matrix4f();
    private FloatBuffer projBuf;
    private Matrix4x3f model = new Matrix4x3f();
    private FloatBuffer modelBuf;
    float time = 0;

    Mesh testMesh;

    private static BGFXReleaseFunctionCallback releaseMemoryCb = BGFXReleaseFunctionCallback.create((_ptr, _userData) -> nmemFree(_ptr));

    // end test data


    public static void main(String[] args) {

        //Compile shaders
        //ShaderCompiler.compile("dev/shaders/src");


        Application.Config config = new Config();
        config.width = 1920;
        config.height = 1080;
        config.title = "Rails";
        config.renderer = "vulkan";

        Rails rails = new Rails();
        rails.start(config);

        rails.shutdown();
    }

    @Override
    public void drawEvent(double delta) {

        bgfx_touch(0);

        bgfx_dbg_text_clear(0,false);

        bgfx_dbg_text_printf(80,15,0x1f,"lol bums");

        bgfx_dbg_text_printf(0, 1, 0x1f, "Rails Debug");
        bgfx_dbg_text_printf(0, 2, 0x3f, "Description: ");

        bgfx_dbg_text_printf(0, 3, 0x0f, "Color can be changed with ANSI \u001b[9;me\u001b[10;ms\u001b[11;mc\u001b[12;ma\u001b[13;mp\u001b[14;me\u001b[0m code too.");

        bgfx_dbg_text_printf(80, 4, 0x0f, "\u001b[;0m    \u001b[;1m    \u001b[; 2m    \u001b[; 3m    \u001b[; 4m    \u001b[; 5m    \u001b[; 6m    \u001b[; 7m    \u001b[0m");
        bgfx_dbg_text_printf(80, 10, 0x0f, "\u001b[;8m    \u001b[;9m    \u001b[;10m    \u001b[;11m    \u001b[;12m    \u001b[;13m    \u001b[;14m    \u001b[;15m    \u001b[0m");

        BgfxUtilities.lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, -35.0f), view);
        BgfxUtilities.perspective(60.0f, 1920, 1080, 0.1f, 100.0f, proj);

        bgfx_set_view_transform(0, view.get4x4(viewBuf), proj.get(projBuf));

        bgfx_dbg_text_printf(0, 3, 0x0f, String.format("Frame: %7.3f[ms]", delta));

        long encoder = bgfx_encoder_begin(false);
        for (int yy = 0; yy < 11; ++yy) {
            for (int xx = 0; xx < 11; ++xx) {
                bgfx_encoder_set_transform(encoder,
                        model.translation(
                                -15.0f + xx * 3.0f,
                                -15.0f + yy * 3.0f,
                                0.0f)
                                .rotateXYZ(
                                        time + xx * 0.21f,
                                        time + yy * 0.37f,
                                        0.0f)
                                .get4x4(modelBuf));

                bgfx_encoder_set_vertex_buffer(encoder, 0, testMesh.vbh, 0, 8);
                bgfx_encoder_set_index_buffer(encoder, testMesh.ibh, 0, 36);

                bgfx_encoder_set_state(encoder, BGFX_STATE_DEFAULT, 0);

                bgfx_encoder_submit(encoder, 0, program, 0, 0);
                time+= 0.0001;
            }
        }
        bgfx_encoder_end(encoder);


        // Advance to next frame. Rendering thread will be kicked to
        // process submitted rendering primitives.
        bgfx_frame(false);
    }

    @Override
    public void logicEvent(double delta) {

    }

    @Override
    public void beforeStart(Application application) {

        Model testModel = Model.fromFile("dev/samples/test.obj");
        testMesh = testModel.meshes[0];

        try {
            Texture texture = new Texture("tex.png","uh");
            texture.create();
/*            testMesh = new Mesh(cubeVertices, cubeIndices, texture)
                    .setVertexLayout(Mesh.VertexType.POSITION, Mesh.VertexType.COLOR)
                    .create();*/

            short vs = BgfxUtilities.loadShader("vs_cubes");
            short fs = BgfxUtilities.loadShader("fs_cubes");

            program = bgfx_create_program(vs, fs, true);

            viewBuf = MemoryUtil.memAllocFloat(16);
            projBuf = MemoryUtil.memAllocFloat(16);
            modelBuf = MemoryUtil.memAllocFloat(16);
        }
        catch (Exception e){
            throw new RuntimeException("Could not load a thing...");
        }
    }
}

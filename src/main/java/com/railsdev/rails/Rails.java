package com.railsdev.rails;

import com.railsdev.rails.core.context.Application;
import com.railsdev.rails.core.context.CoreApplication;
import com.railsdev.rails.core.render.*;
import com.railsdev.rails.core.render.debug.DebugCube;
import com.railsdev.rails.core.render.debug.DebugCubeTex;
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

import static org.lwjgl.glfw.GLFW.*;


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

    private short uniformTexColor;
    private short uniformTexNormal;

    private Vector3f camera = new Vector3f(0.0f,0.0f,3.0f);
    private Vector3f cameraTarget = new Vector3f(0.0f,0.0f,0.0f);

    private short textureColor;
    private Matrix4x3f view = new Matrix4x3f();     // View transformation matrix -- Transformation of vertices relative to camera space
    private FloatBuffer viewBuf;
    private Matrix4f proj = new Matrix4f();         // Projection transformation matrix (perpective) -- Transformation to clip space
    private FloatBuffer projBuf;
    private Matrix4x3f model = new Matrix4x3f();    // Model transformation matrix -- Transformation of object relative to world space
    private FloatBuffer modelBuf;
    float time = 0;

    float speedx = 0;
    float speedy = 0;
    float speedz = 0;
    boolean debug = true;

    Camera cameraObject;

    Mesh testMesh;

    private static BGFXReleaseFunctionCallback releaseMemoryCb = BGFXReleaseFunctionCallback.create((_ptr, _userData) -> nmemFree(_ptr));

    // end test data


    public static void main(String[] args) {

        //Compile shaders
        //ShaderCompiler.compile("dev/shaders/src", "spirv");


        Application.Config config = new Config();
        config.width = 1920;
        config.height = 1080;
        config.title = "Rails";
        config.renderer = "vulkan";


        Rails rails = new Rails();
        rails.start(config);
        rails.shutdown();

    }

    boolean firstMouse = true;
    double lastX;
    double lastY;
    void mouseCallback(long window, double x, double y){
        if (firstMouse) {
            lastX = x;
            lastY = y;
            firstMouse = false;
        }
        float xoff = (float) (x -lastX);
        float yoff = (float) (y-lastY);

        lastY = y;
        lastX = x;

        System.out.println(String.format("X %f Y %f",x,y));
        cameraObject.processMouseMove(-xoff,yoff, true);

    }

    void resize(long window, int width, int height){
        bgfx_reset(width,height,BGFX_RESET_VSYNC,0);
    }

    @Override
    public void drawEvent(double delta) {
        //bgfx_touch(0);
        if (debug) {

            bgfx_dbg_text_clear(0, false);

            bgfx_dbg_text_printf(80, 15, 0x1f, "lol bums");

            bgfx_dbg_text_printf(0, 1, 0x1f, "Rails Debug");
            bgfx_dbg_text_printf(0, 2, 0x3f, String.format("Camera: z % 7.3f[ms] x % 7.3f[ms] y % 7.3f[ms] X: %f Y: %f Z = %f", speedz, speedx, speedy, cameraObject.position.x, cameraObject.position.y, cameraObject.position.z));

            bgfx_dbg_text_printf(0, 3, 0x0f, "Color can be changed with ANSI \u001b[9;me\u001b[10;ms\u001b[11;mc\u001b[12;ma\u001b[13;mp\u001b[14;me\u001b[0m code too.");

            bgfx_dbg_text_printf(80, 4, 0x0f, "\u001b[;0m    \u001b[;1m    \u001b[; 2m    \u001b[; 3m    \u001b[; 4m    \u001b[; 5m    \u001b[; 6m    \u001b[; 7m    \u001b[0m");
            bgfx_dbg_text_printf(80, 10, 0x0f, "\u001b[;8m    \u001b[;9m    \u001b[;10m    \u001b[;11m    \u001b[;12m    \u001b[;13m    \u001b[;14m    \u001b[;15m    \u001b[0m");
        }
/*        Vector3f camera = new Vector3f(0.0f,0.0f,3.0f);
        Vector3f cameraTarget = new Vector3f(0.0f,0.0f,0.0f);
        Vector3f cameraDirection = camera.sub(cameraTarget).normalize();
        Vector3f up = new Vector3f(0.0f,1.0f,0.0f);
        Vector3f cameraRight = up.cross(cameraDirection).normalize();
        Vector3f cameraUp = cameraDirection.cross(cameraRight);*/

        // Calculate view matrix
        //BgfxUtilities.lookAt(cameraTarget, camera, view);

        // Calculate projection matrix
        BgfxUtilities.perspective(60.0f, 1920, 1080, 0.1f, 100.0f, proj);

        // Send matrices to bgfx
        bgfx_set_view_transform(0, cameraObject.getViewMatrix(view).get4x4(viewBuf), proj.get(projBuf));

        long encoder = bgfx_encoder_begin(false);
        bgfx_encoder_set_transform(encoder, model.rotateXYZ(0,(1 * 0.01f),0).get4x4(modelBuf));

        bgfx_encoder_set_vertex_buffer(encoder, 0, testMesh.vbh, 0, 8);
        bgfx_encoder_set_index_buffer(encoder, testMesh.ibh, 0, 36);

        //Bind textures
        bgfx_encoder_set_texture(encoder,0,uniformTexColor,textureColor,0xffffffff);

        bgfx_encoder_set_state(encoder, BGFX_STATE_DEFAULT | BGFX_STATE_CULL_CCW, 0); //TODO for openGL cull mode needs to be CW

        bgfx_encoder_submit(encoder, 0, program, 0, 0);
                time+= 0.0001;
        bgfx_encoder_end(encoder);


        // Advance to next frame. Rendering thread will be kicked to
        // process submitted rendering primitives.
        bgfx_frame(false);
    }

    @Override
    public void logicEvent(double delta) {
        float acceleration = 0.01f;
        float cameraSpeed = 0.5f;

        //cameraObject.processKeyboard(Camera.CameraMovement.LEFT,0.1f);

        // Left - right
        if(glfwGetKey(this.window,GLFW_KEY_A) == GLFW_PRESS)
            speedz+= (speedz < cameraSpeed) ? acceleration : 0;
        if(glfwGetKey(this.window,GLFW_KEY_A) == GLFW_RELEASE)
            speedz-= (speedz > 0) ? acceleration : 0;

        if(glfwGetKey(this.window,GLFW_KEY_D) == GLFW_PRESS)
            speedz-= (speedz > -cameraSpeed) ? acceleration : 0;
        if(glfwGetKey(this.window,GLFW_KEY_D) == GLFW_RELEASE)
            speedz+= (speedz < 0) ? acceleration : 0;

        // Forward - backward
        if(glfwGetKey(this.window,GLFW_KEY_W) == GLFW_PRESS)
            speedx+= (speedx < cameraSpeed) ? acceleration : 0;
        if(glfwGetKey(this.window,GLFW_KEY_W) == GLFW_RELEASE)
            speedx-= (speedx > 0.1) ? acceleration : 0;

        if(glfwGetKey(this.window,GLFW_KEY_S) == GLFW_PRESS)
            speedx-= (speedx > -cameraSpeed) ? acceleration : 0;
        if(glfwGetKey(this.window,GLFW_KEY_S) == GLFW_RELEASE)
            speedx+= (speedx < 0) ? acceleration : 0;


        if(glfwGetKey(this.window,GLFW_KEY_Q) == GLFW_PRESS){
            camera.y += cameraSpeed;
        }
        if(glfwGetKey(this.window,GLFW_KEY_E) == GLFW_PRESS) {
            camera.y -= cameraSpeed;
        }

        camera.x += speedx;
        camera.z += speedz;
        camera.y += speedy;
        cameraObject.processKeyboard(Camera.CameraMovement.RIGHT,speedz);
        cameraObject.processKeyboard(Camera.CameraMovement.FORWARD,speedx);
    }

    @Override
    public void beforeStart(Application application) {

        Model testModel = Model.fromFile("dev/samples/test.obj");
        //testMesh = testModel.meshes[0];
        //testMesh = new DebugCube().create();
        testMesh = new DebugCubeTex().create();

        cameraObject = new Camera(new Vector3f(0.0f,0.0f,3.0f),new Vector3f(0.0f,1.0f,0.0f));

        try {

            uniformTexColor = bgfx_create_uniform("s_texColor", BGFX_UNIFORM_TYPE_VEC4,0);
            textureColor = BgfxUtilities.loadTexture("tex2.dds");

            short vs = BgfxUtilities.loadShader("vs_rBasicUnlit");
            short fs = BgfxUtilities.loadShader("fs_rBasicUnlit");

            program = bgfx_create_program(vs, fs, true);

            viewBuf = MemoryUtil.memAllocFloat(16);
            projBuf = MemoryUtil.memAllocFloat(16);
            modelBuf = MemoryUtil.memAllocFloat(16);
        }
        catch (Exception e){
            throw new RuntimeException("Could not load a thing...");
        }

        glfwSetCursorPosCallback(window, this::mouseCallback);

        glfwSetFramebufferSizeCallback(window, this::resize);
    }
}

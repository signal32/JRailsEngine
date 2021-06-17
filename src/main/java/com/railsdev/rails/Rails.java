package com.railsdev.rails;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.railsdev.rails.core.context.Application;
import com.railsdev.rails.core.context.CoreApplication;
import com.railsdev.rails.core.render.*;
import com.railsdev.rails.core.render.debug.DebugCubeTex;
import com.railsdev.rails.core.render.shaders.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.joml.Matrix4f;
import org.joml.Matrix4x3f;
import org.joml.Vector3f;
import org.lwjgl.bgfx.BGFXAttachment;
import org.lwjgl.bgfx.BGFXReleaseFunctionCallback;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.bgfx.BGFX.*;
import static org.lwjgl.system.MemoryUtil.nmemFree;
import static org.lwjgl.glfw.GLFW.*;


public class Rails extends CoreApplication{

    private static final Logger LOGGER = LogManager.getLogger(Rails.class);




    private short uniformLightPositions;
    private short uniformLightColours;
    private ByteBuffer uniformBuf;

    private short uniformCameraPos;
    private FloatBuffer cameraPosBuffer;

    private short cubeMapFB;
    private short[] cubemapTextures = new short[6];
    private short cubeMapTex;
    private short[] cubeMapFBs = new short[6];
    private BGFXAttachment cubeMapAt;
    private Matrix4x3f[] cubeCaptureViews= new Matrix4x3f[6];
    private Shader cubeMapShader;


    private Matrix4x3f view = new Matrix4x3f();     // View transformation matrix -- Transformation of vertices relative to camera space
    private FloatBuffer viewBuf;
    private Matrix4f proj = new Matrix4f();         // Projection transformation matrix (perpective) -- Transformation to clip space
    private FloatBuffer projBuf;
    private Matrix4x3f model = new Matrix4x3f();    // Model transformation matrix -- Transformation of object relative to world space
    private FloatBuffer modelBuf;
    float time = 0;

    double speedx = 0;
    double speedy = 0;
    double speedz = 0;
    boolean debug = true;

    private static final float[][] lightRgbInnerR = {
            { 150.0f, 150.0f, 255.0f },
            { 0.0f, 0.0f, 150.0f },
            { 150.0f, 150.0f, 150.0f },
            { 150.0f, 150.0f, 150.0f },
    };
    private static final float[][] lightPos = {
            { 0.0f, 0.0f, 10.0f },
            { 0.0f, 0.0f, 30.0f },
            { 0.0f, 0.0f, 10.0f },
            { 0.0f, 0.0f, 10.0f },
    };

    Camera cameraObject;

    Mesh testMesh;
    Shader testShader;

    Mesh testMesh2;
    Shader testShader2;

    Model testModel;
    Shader debugShader;

    Shader skybox;

    private static BGFXReleaseFunctionCallback releaseMemoryCb = BGFXReleaseFunctionCallback.create((_ptr, _userData) -> nmemFree(_ptr));

    // end test data

    @Parameter(names = {"-log"})    private String log              ="debug";
    @Parameter(names = {"-render"}) private String renderString     ="vk";


    public static void main(String[] args) {

        String[] argv = {"-log","warn","-render","vk"};
        JCommander.newBuilder()
                .addObject(new Rails())
                .build()
                .parse(args);

        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration logConfig = ctx.getConfiguration();
        LoggerConfig loggerConfig = logConfig.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(Level.ALL);
        ctx.updateLoggers();  // This causes all Loggers to refetch information from their LoggerConfig.

        //Compile shaders
        //ShaderCompiler.compile("dev/shaders/src", "spirv");

        LOGGER.info("Hello from Rails!");
        LOGGER.info("Logging Level: {}", loggerConfig.getLevel());

        Application.Config config = new Config();
        config.width = 1920;
        config.height = 1080;
        config.title = "Rails";
        config.renderer = "vulkan";


        Rails rails = new Rails();
        rails.start(config);
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

        if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_2) == GLFW_PRESS){
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
            cameraObject.processMouseMove(-xoff,yoff, true);
        }
        else{
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        }
    }

    void resize(long window, int width, int height){
        bgfx_reset(width,height,BGFX_RESET_VSYNC,0);
    }

    @Override
    public void drawStart(double delta) {
        //bgfx_touch(0);
        if (debug) {

            //bgfx_dbg_text_clear(0, false);

            bgfx_dbg_text_printf(0, 0, 0x1f, "Rails Debug");
            bgfx_dbg_text_printf(0, 1, 0x3f, String.format("Camera: z % 7.3f[ms] x % 7.3f[ms] y % 7.3f[ms] X: %f Y: %f Z = %f", speedz, speedx, speedy, cameraObject.position.x, cameraObject.position.y, cameraObject.position.z));
            bgfx_dbg_text_printf(0, 2, 0x3f, String.format("Time: % 7.3f", time));
            bgfx_dbg_text_printf(0, 3, 0x0f, "Color can be changed with ANSI \u001b[9;me\u001b[10;ms\u001b[11;mc\u001b[12;ma\u001b[13;mp\u001b[14;me\u001b[0m code too.");
            bgfx_dbg_text_printf(100, 0, 0x0f, "\u001b[;0m    \u001b[;1m    \u001b[; 2m    \u001b[; 3m    \u001b[; 4m    \u001b[; 5m    \u001b[; 6m    \u001b[; 7m    \u001b[0m");
            //bgfx_dbg_text_printf(100, 1, 0x0f, "\u001b[;8m    \u001b[;9m    \u001b[;10m    \u001b[;11m    \u001b[;12m    \u001b[;13m    \u001b[;14m    \u001b[;15m    \u001b[0m");
        }

        // SETUP DEFERRED
        //BgfxUtilities.perspective(90.0f, 512, 512, 0.1f, 100.0f, proj);

        // Calculate projection matrix
        BgfxUtilities.perspective(60.0f, 1920, 1080, 0.1f, 100.0f, proj);

        // Send matrices to bgfx
        bgfx_set_view_transform(Renderer.RENDER_PASS_SCENE, cameraObject.getViewMatrix(view).get4x4(viewBuf), proj.get(projBuf));

        long encoder = bgfx_encoder_begin(false);

        bgfx_encoder_set_transform(encoder, model.get4x4(modelBuf));

        // TEMP - Set camerea position
        cameraPosBuffer.clear();
        float[] pos = {cameraObject.position.x,cameraObject.position.y,cameraObject.position.z};
        for (float f : pos){
            cameraPosBuffer.put(f);
        }
        cameraPosBuffer.flip();
        bgfx_encoder_set_uniform(encoder,uniformCameraPos,cameraPosBuffer,1);

        // TEMP - Set light positions
        uniformBuf.clear();
        for (float[] ll : lightPos) {
            for (float l : ll) {

                uniformBuf.putFloat(l);
            }
        }
        uniformBuf.flip();
        bgfx_encoder_set_uniform(encoder, uniformLightPositions, uniformBuf, 4);

        // TEMP - Set light colours
        uniformBuf.clear();
        for (float[] ll : lightRgbInnerR) {
            for (float l : ll) {
                uniformBuf.putFloat(l);
            }
        }
        uniformBuf.flip();
        bgfx_encoder_set_uniform(encoder, uniformLightColours, uniformBuf, 4);

        bgfx_encoder_set_vertex_buffer(encoder,0,testMesh.vbh,0,36);
        bgfx_encoder_set_index_buffer(encoder,testMesh.ibh,0,36);
        bgfx_encoder_set_texture(encoder,0,skybox.getUniform(0),cubeMapTex, BGFX_SAMPLER_UVW_MIRROR);
        bgfx_encoder_set_state(encoder, BGFX_STATE_DEFAULT | BGFX_STATE_CULL_CCW, 0);
        bgfx_encoder_submit(encoder, Renderer.RENDER_PASS_SCENE, skybox.id(), 0, 0);


        //testMesh.draw(encoder,testShader,Renderer.RENDER_PASS_SCENE);
        //testMesh2.draw(encoder,testShader2);
        testModel.draw(encoder,debugShader);

        bgfx_encoder_end(encoder);

        // Advance to next frame. Rendering thread will be kicked to
        // process submitted rendering primitives.
        bgfx_frame(false);
        time+= 0.1;
    }

    @Override
    public void drawEnd(double delta) {

    }

    double timeThen = System.currentTimeMillis();
    @Override
    public void update(double delta) {
        //double timeNow = System.currentTimeMillis();
        //double el = 1/(timeNow- timeThen);
        //this.renderer.pushDebugText(100,1,String.format("time %f",el));
        model.rotateXYZ(0,(1 * 0.001f),0);

        float acceleration = 0.1f;
        float cameraSpeed = 1.5f;

        // Left - right
        if(glfwGetKey(this.mainWindow.id(),GLFW_KEY_A) == GLFW_PRESS)
            speedz+= (speedz < cameraSpeed) ? acceleration : 0;
        if(glfwGetKey(this.mainWindow.id(),GLFW_KEY_A) == GLFW_RELEASE)
            speedz-= (speedz-acceleration > 0) ? acceleration : 0;

        if(glfwGetKey(this.mainWindow.id(),GLFW_KEY_D) == GLFW_PRESS)
            speedz-= (speedz > -cameraSpeed) ? acceleration : 0;
        if(glfwGetKey(this.mainWindow.id(),GLFW_KEY_D) == GLFW_RELEASE)
            speedz+= (speedz+acceleration < 0) ? acceleration : 0;

        // Forward - backward
        if(glfwGetKey(this.mainWindow.id(),GLFW_KEY_W) == GLFW_PRESS)
            speedx+= (speedx < cameraSpeed) ? acceleration : 0;
        if(glfwGetKey(this.mainWindow.id(),GLFW_KEY_W) == GLFW_RELEASE)
            speedx-= (speedx > 0) ? acceleration : 0;

        if(glfwGetKey(this.mainWindow.id(),GLFW_KEY_S) == GLFW_PRESS)
            speedx-= (speedx > -cameraSpeed) ? acceleration : 0;
        if(glfwGetKey(this.mainWindow.id(),GLFW_KEY_S) == GLFW_RELEASE)
            speedx+= (speedx+acceleration < 0) ? acceleration : 0;

        // up - down
        if(glfwGetKey(this.mainWindow.id(),GLFW_KEY_Q) == GLFW_PRESS)
            speedy+= (speedy < cameraSpeed) ? acceleration : 0;
        if(glfwGetKey(this.mainWindow.id(),GLFW_KEY_Q) == GLFW_RELEASE)
            speedy-= (speedy > 0.1) ? acceleration : 0;

        if(glfwGetKey(this.mainWindow.id(),GLFW_KEY_E) == GLFW_PRESS)
            speedy-= (speedy > -cameraSpeed) ? acceleration : 0;
        if(glfwGetKey(this.mainWindow.id(),GLFW_KEY_E) == GLFW_RELEASE)
            speedy+= (speedy < 0) ? acceleration : 0;

        if(glfwGetKey(this.mainWindow.id(),GLFW_KEY_TAB) == GLFW_PRESS) {
            speedz = 0;
            speedx = 0;
        }
        if(glfwGetKey(this.mainWindow.id(),GLFW_KEY_R) == GLFW_PRESS) {
            cameraObject.position.x = 0;
            cameraObject.position.y = 0;
            cameraObject.position.z = 0;
        }


        cameraObject.processKeyboard(Camera.CameraMovement.RIGHT, (float) (speedz * delta));
        cameraObject.processKeyboard(Camera.CameraMovement.FORWARD, (float) (speedx * delta));
    }

    @Override
    public void shutdown(Application application) {

    }

    @Override
    public void init(Application application) {
        try {

            String[] textures = {
                    "metal/color.dds",
                    "metal/normal.dds",
                    "metal/metal.dds",
                    "metal/rough.dds",
                    "metal/ao.dds"};

            //testModel = Model.fromFile("dev/samples/model/SkiLiftGround_Tower.obj");
            testModel = Model.fromFile("dev/samples/cubeMaped.obj");
            //testMesh2 = testModel.meshes[1];
            //testMesh = new DebugCube().create();

            // PBR DEMO OBJECTS
            //testMesh = new DebugCubeTex(textures).create();
            //testShader = new PhysicallyBasedShader().create();

            // CUBEMAP DEMO OBJECTS
            testMesh = new DebugCubeTex("cubemap/cubemap.dds").create();
            testShader = new CubemapShader().create();

                //testMesh2 = new DebugCubeTex(textures).create();
            testShader2 = new PhysicallyBasedShader().create();

            cameraObject = new Camera(new Vector3f(0.0f,0.0f,3.0f),new Vector3f(0.0f,1.0f,0.0f));

            debugShader = new DebugShader().create();

            uniformLightColours = bgfx_create_uniform("lightColors",BGFX_UNIFORM_TYPE_VEC4,4);
            uniformLightPositions = bgfx_create_uniform("lightPositions",BGFX_UNIFORM_TYPE_VEC4,4);
            uniformCameraPos = bgfx_create_uniform("cameraPos",BGFX_UNIFORM_TYPE_VEC4,1);



            viewBuf = MemoryUtil.memAllocFloat(16);
            projBuf = MemoryUtil.memAllocFloat(16);
            modelBuf = MemoryUtil.memAllocFloat(16);
            uniformBuf = MemoryUtil.memAlloc(16 * 4);
            cameraPosBuffer = MemoryUtil.memAllocFloat(4);

            long flags =
                      BGFX_TEXTURE_RT
                    | BGFX_SAMPLER_MIN_POINT
                    | BGFX_SAMPLER_MAG_POINT
                    | BGFX_SAMPLER_MIP_POINT
                    | BGFX_SAMPLER_U_CLAMP
                    | BGFX_SAMPLER_V_CLAMP;

            // Create cubemap shader
            cubeMapShader = new CubemapShader().create();

            // Create empty textures for cubemap -- objects can use these textures to draw the contents of the fb
/*
            for (int i = 0; i< cubemapTextures.length; i++)
                cubemapTextures[i] = bgfx_create_texture_2d(512,512,false,1,BGFX_TEXTURE_FORMAT_BGRA8,flags,null);
*/


            cubeMapTex = bgfx_create_texture_cube(512,false,6,BGFX_TEXTURE_FORMAT_BGRA8,flags,null); //new

            for (short i = 0; i < cubeMapFBs.length; i++){
                BGFXAttachment at = BGFXAttachment.create();

                bgfx_attachment_init(at,cubeMapTex,BGFX_ACCESS_READWRITE,i,1,1,BGFX_RESOLVE_NONE);
                BGFXAttachment.Buffer atBuff = BGFXAttachment.create(1024);
                //atBuff.put(at);
                atBuff.handle(cubeMapTex).access(BGFX_ACCESS_READWRITE).layer((short) 0);
                atBuff.flip();

                cubeMapFBs[i] = bgfx_create_frame_buffer_from_attachment(atBuff,true);
                //cubeMapFBs[i] = bgfx_create_frame_buffer_from_handles(new short[cubeMapTex], true);

            }

            // Create framebuffer with reference to textures
            //cubeMapFB = bgfx_create_frame_buffer_from_handles(cubemapTextures,true);

            // Create a transformation for camera looking at each of cubes faces
            for (int i = 0; i < cubeCaptureViews.length; i++){
                cubeCaptureViews[i] = new Matrix4x3f();
            }
            Vector3f eye = new Vector3f(3.0f,3.0f,3.0f);
            cubeCaptureViews[0].setLookAtLH(eye,new Vector3f(-5.0f,  0.0f,  2.0f),new Vector3f(0.0f, 1.0f,  0.0f));
            cubeCaptureViews[1].setLookAtLH(eye,new Vector3f(-1.0f,  0.0f,  0.0f),new Vector3f(0.0f, -1.0f,  0.0f));
            cubeCaptureViews[2].setLookAtLH(eye,new Vector3f(0.0f,  1.0f,  0.0f),new Vector3f(0.0f,  0.0f,  1.0f));
            cubeCaptureViews[3].setLookAtLH(eye,new Vector3f( 0.0f, -1.0f,  0.0f),new Vector3f(0.0f,  0.0f, -1.0f));
            cubeCaptureViews[4].setLookAtLH(eye,new Vector3f(0.0f,  0.0f,  1.0f),new Vector3f(0.0f, -1.0f,  0.0f));
            cubeCaptureViews[5].setLookAtLH(eye,new Vector3f(0.0f,  0.0f, -1.0f),new Vector3f(0.0f, -1.0f,  0.0f));
            Camera cubeCam = new Camera(new Vector3f(0.0f,0.0f,0.0f),new Vector3f(0.0f,1.0f,0.0f));

            // Set projection
            BgfxUtilities.perspective(90.0f, 512, 512, 0.1f, 100.0f, proj);


            // Setup the fb view
            bgfx_set_view_rect(Renderer.RENDER_PASS_ENV_MAP,0,0,512,512);
            bgfx_set_view_frame_buffer(Renderer.RENDER_PASS_ENV_MAP,cubeMapFBs[0]);

            bgfx_set_view_transform(Renderer.RENDER_PASS_ENV_MAP, cubeCam.getViewMatrix(view).get4x4(viewBuf), proj.get(projBuf));

            // clear fb
            bgfx_set_view_clear(Renderer.RENDER_PASS_ENV_MAP,BGFX_CLEAR_COLOR | BGFX_CLEAR_DEPTH,0xF94552ff, 1.0f,0);
            bgfx_touch(Renderer.RENDER_PASS_ENV_MAP);

            //render
            long encoder = bgfx_encoder_begin(false);
            //bgfx_encoder_set_transform(encoder, model.get4x4(modelBuf));
            testMesh.draw(encoder,testShader,0);
            bgfx_encoder_end(encoder);
            bgfx_frame(false);
            skybox = new SkyboxShader().create();


        }
        catch (Exception e){
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }

        glfwSetCursorPosCallback(mainWindow.id(), this::mouseCallback);

        glfwSetFramebufferSizeCallback(mainWindow.id(), this::resize);
    }
}

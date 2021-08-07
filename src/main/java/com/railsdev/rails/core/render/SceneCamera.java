package com.railsdev.rails.core.render;

import com.railsdev.rails.core.scene.Drawable;
import org.joml.Matrix4f;
import org.joml.Matrix4x3f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;


import java.nio.FloatBuffer;
import static com.railsdev.rails.core.render.BgfxUtilities.*;
import static org.lwjgl.bgfx.BGFX.*;

/**
 * Camera for drawing {@Link Drawable}.
 * Use in conjunction with {@Link scenegraph} or implement your own.
 */
public class SceneCamera {

    private final Camera        camera;
    private final Drawable[]    drawables;

    private final Matrix4x3f    view;       // View transformation matrix -- Transformation of vertices relative to camera space
    private final FloatBuffer   viewBuf;
    private final Matrix4f      proj;       // Projection transformation matrix (perspective) -- Transformation to clip space
    private final FloatBuffer   projBuf;
    private final Matrix4x3f    worldTransform;     // Model transformation matrix -- Transformation of object relative to world space
    private final FloatBuffer   worldTransformBuf;

    public SceneCamera(Drawable[] drawables){

        this.camera = new Camera(new Vector3f());
        this.drawables = drawables;

        this.view = new Matrix4x3f();
        this.proj = new Matrix4f();
        this.worldTransform = new Matrix4x3f();

        viewBuf  = MemoryUtil.memAllocFloat(16);
        projBuf  = MemoryUtil.memAllocFloat(16);
        worldTransformBuf = MemoryUtil.memAllocFloat(16);
    }

    public SceneCamera translate(Vector3f translation){
        worldTransform.translate(translation.negate());
        return this;
    }

    /**
     * Draw all drawables belonging to this camera
     * @param viewId Destination view
     */
    public void draw(int viewId){

        long encoder = bgfx_encoder_begin(false);

        perspective(60.0f,1920,1080,0.1f,100.0f, proj);
        bgfx_set_view_transform(viewId,camera.getViewMatrix(view).get4x4(viewBuf),proj.get(projBuf));

        // model matrix from world
        bgfx_encoder_set_transform(encoder,worldTransform.get4x4(worldTransformBuf));

        for (Drawable drawable : drawables){
            drawable.draw(encoder, viewId);
        }

        bgfx_encoder_end(encoder);
    }
}

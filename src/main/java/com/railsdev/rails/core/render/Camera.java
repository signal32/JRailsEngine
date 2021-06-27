package com.railsdev.rails.core.render;

import org.joml.Matrix4x3f;
import org.joml.Vector3f;

import static org.joml.Math.*;
import static org.joml.Math.toRadians;

public class Camera {

    private static final float[] UP_VEC = {0.0f,1.0f,0.0f};

    // interface
    public final Vector3f eyePos;
    public final Vector3f atPos;
    public float    yaw   = -90.0f;
    public float    pitch = 0.0f;

    // generated
    protected final Vector3f front   = new Vector3f();
    protected final Vector3f right   = new Vector3f();
    protected final Vector3f worldUp = new Vector3f(UP_VEC);
    protected       Vector3f localUp = new Vector3f();


    /**
     * Create a new camera
     * @param eyePos - position of the eye
     * @param atPos - position camera is looking at
     * @param worldUp - normalised vector representing worlds positive up axis
     */
    public Camera(Vector3f eyePos, Vector3f atPos, Vector3f worldUp){
        this.eyePos = eyePos;
        this.atPos = atPos;
        this.worldUp.set(worldUp);

        updateVectors();
    }

    /**
     * Create a new camera
     * @param eyePos - position of the eye
     * @param atPos - position camera is looking at
     */
    public Camera(Vector3f eyePos, Vector3f atPos){
        this.eyePos = eyePos;
        this.atPos = atPos;

        updateVectors();
    }

    /**
     * Create a new camera
     * @param eyePos - position of the eye
     */
    public Camera(Vector3f eyePos){
        this(eyePos, new Vector3f());
    }

    /**
     * View matrix
     * @param dest matrix to which view matrix will be copied to
     * @return same as dest
     */
    public Matrix4x3f getViewMatrix(Matrix4x3f dest){
        return dest.setLookAtLH(eyePos, atPos, localUp);
    }

    public void updateVectors(){

        // Calculate new front vector
        front.x = cos(toRadians(yaw)) * cos(toRadians(pitch));
        front.y = sin(toRadians(pitch));
        front.z = sin(toRadians(yaw)) * cos(toRadians(pitch));
        front.normalize();

        // Re-calculate right and up vectors (looks slightly convoluted but avoids using 'new')
        right.set(front);
        right.cross(worldUp).normalize();
        localUp.set(right);
        localUp = localUp.cross(front).normalize();
        atPos.set(eyePos);
        atPos.add(front);
    }
}

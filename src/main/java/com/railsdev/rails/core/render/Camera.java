package com.railsdev.rails.core.render;

import org.joml.Math;
import org.joml.Vector3f;
import  org.joml.*;
import static org.joml.Math.*;

public class Camera {

    public enum CameraMovement {
        FORWARD,
        BACKWARD,
        LEFT,
        RIGHT
    };

    // Attributes
    public Vector3f position;
    Vector3f front;
    Vector3f up;
    Vector3f right;
    Vector3f worldUp;
    Vector3f at;
    private Matrix4x3f view;

    // Euler Angles
    float yaw   = -90.0f;
    float pitch = 0.0f;

    // User Options
    float movementSpeed     = 2.5f;
    float mouseSensitivity  = 0.1f;
    float zoom              = 45.0f;

    public Camera(Vector3f position, Vector3f up){
        this.position = position;
        this.worldUp = up;
        this.front = new Vector3f();
        this.up = new Vector3f();
        this.right = new Vector3f();
        this.at = new Vector3f();
        updateVectors();
    }

    public Matrix4x3f getViewMatrix(Matrix4x3f dest){
        return dest.setLookAtLH(position,at,up);
    }

    public void processKeyboard(CameraMovement movement, float detaTime){
        float velocity = movementSpeed * detaTime;
        switch (movement){
            case FORWARD:
                position.add(front.mul(velocity));
            case BACKWARD:
                position.sub(front.mul(velocity));
            case LEFT:
                position.sub(right.mul(velocity));
            case RIGHT:
                position.add(right.mul(velocity));
        }
        updateVectors();
    }

    public void processMouseMove(float xoffset, float yoffset, boolean constrainPitch){
        xoffset *= mouseSensitivity;
        yoffset *= mouseSensitivity;

        yaw += xoffset;
        pitch += yoffset;

        // Stop the screen from getting flipped :D
        if (constrainPitch) {
            if (pitch > 89.0f)  pitch = 89.0f;
            if (pitch < -89.0f) pitch = -89.0f;
        }

        updateVectors();
    }

    public void processMouseScroll(float offset){
        zoom -= offset;

        if (zoom < 1.0f) zoom = 1.0f;
        if (zoom > 45.0f) zoom = 45.0f;
    }

    private void updateVectors(){
        // Calculate new front vector
        //front.zero();
        front.x = cos(toRadians(yaw)) * cos(toRadians(pitch));
        front.y = sin(toRadians(pitch));
        front.z = sin(toRadians(yaw)) * cos(toRadians(pitch));
        front.normalize();

        // Re-calculate right and up vectors (looks slightly convoluted but avoids using 'new')
        right.set(front);
        right.cross(worldUp).normalize();
        up.set(right);
        up = up.cross(front).normalize();
        at.set(position);
        at.add(front);
    }
}

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
    Vector3f position;
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
        updateVectors();
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
        Vector3f front = new Vector3f();
        front.x = cos(toRadians(yaw) * cos(toRadians(pitch)));
        front.y = sin(toRadians(pitch));
        front.z = sin(toRadians(yaw) * cos(toRadians(pitch)));
        front.normalize();

        // Re-calculate right and up vectors
        Vector3f frontCopy = new Vector3f(front); //TODO needs some work to reduce heap allocation
        right = front.cross(worldUp).normalize();
        up = right.cross(frontCopy).normalize();
    }

    public void getViewMatrix(Matrix4x3f dest){
        dest.setLookAtLH(position,at,up);
//TODO this wont work because position will be overwrittern

    }



}

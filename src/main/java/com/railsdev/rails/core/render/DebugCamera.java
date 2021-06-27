package com.railsdev.rails.core.render;

import org.joml.Vector3f;
import  org.joml.*;
import static org.joml.Math.*;

public class DebugCamera extends Camera {

    public enum CameraMovement {
        FORWARD,
        BACKWARD,
        LEFT,
        RIGHT
    }

    // User Options
    float movementSpeed     = 2.5f;
    float mouseSensitivity  = 0.1f;
    float zoom              = 45.0f;

    public DebugCamera(Vector3f position, Vector3f up){
        super(position);
    }

    public void processKeyboard(CameraMovement movement, float detaTime){
        float velocity = movementSpeed * detaTime;
        switch (movement){
            case FORWARD:
                eyePos.add(front.mul(velocity)); break;
            case BACKWARD:
                eyePos.sub(front.mul(velocity)); break;
            case RIGHT:
                eyePos.sub(right.mul(velocity)); break;
            case LEFT:
                eyePos.add(right.mul(velocity)); break;
        }
        updateVectors();
    }

    public void processMouseMove(float xoffset, float yoffset, boolean constrainPitch){
        xoffset *= mouseSensitivity;
        yoffset *= mouseSensitivity;

        yaw -= xoffset;
        pitch -= yoffset;

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
}

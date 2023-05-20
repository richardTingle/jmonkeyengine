package com.jme3.actions;

import org.lwjgl.openxr.XR10;

public enum ActionType{
    BOOLEAN(XR10.XR_ACTION_TYPE_BOOLEAN_INPUT),
    FLOAT(XR10.XR_ACTION_TYPE_FLOAT_INPUT),
    VECTOR2F(XR10.XR_ACTION_TYPE_VECTOR2F_INPUT),
    POSE(XR10.XR_ACTION_TYPE_POSE_INPUT),
    /**
     * Vibrating the controller
     */
    HAPTIC(XR10.XR_ACTION_TYPE_VIBRATION_OUTPUT);

    private final int openXrOption;

    ActionType(int openXrOption){
        this.openXrOption = openXrOption;
    }

    public int getOpenXrOption(){
        return openXrOption;
    }
}

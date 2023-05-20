package com.jme3.actions.controllerprofile;

/**
 * This is not a real controller, it is a basic profile that provides basic pose, button, and haptic support for
 * applications with simple input needs.
 */
public class KronosSimpleController{
    public static final String PROFILE = "/interaction_profiles/khr/simple_controller";

    public static class InteractionProfiles{
        public static final String LEFT_HAND = "/user/hand/left";

        public static final String RIGHT_HAND ="/user/hand/right";
    }

    public static class ComponentPaths{
        public static final String CLICK ="/input/select/click";
        public static final String MENU_CLICK ="/input/menu/click";
        public static final String GRIP_POSE ="/input/grip/pose";
        public static final String AIM_POSE ="/input/aim/pose";
        public static final String HAPTIC ="/output/haptic";
    }

}

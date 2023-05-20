package com.jme3.actions.controllerprofile.oculus;

public class MixedRealityMotionController{
    public static final String PROFILE = "/interaction_profiles/microsoft/motion_controller";

    public static class InteractionProfiles{
        public static final String LEFT_HAND = "/user/hand/left";
        public static final String RIGHT_HAND ="/user/hand/right";
    }

    public static class ComponentPaths{
        public static final String MENU_CLICK ="/input/menu/click";
        public static final String SQUEEZE_CLICK ="/input/squeeze/click";
        public static final String TRIGGER_VALUE ="/input/trigger/value";
        public static final String THUMB_STICK_X ="/input/thumbstick/x";
        public static final String THUMB_STICK_Y ="/input/thumbstick/y";
        public static final String THUMB_STICK_CLICK ="/input/thumbstick/click";
        public static final String TRACKPAD_X ="/input/trackpad/x";
        public static final String TRACKPAD_Y ="/input/trackpad/y";
        public static final String TRACKPAD_CLICK ="/input/trackpad/click";
        public static final String TRACKPAD_TOUCH ="/input/trackpad/touch";
        public static final String GRIP_POST ="/input/grip/pose";
        public static final String AIM_POSE ="/input/aim/pose";
        public static final String HAPTIC ="/output/haptic";
    }

    public static BindingPathBuilder pathBuilder(){
        return new BindingPathBuilder();
    }

    public static class BindingPathBuilder{
        public BindingPathBuilderHand hand(String handPath){
            return new BindingPathBuilderHand(handPath);
        }
    }

    public static class BindingPathBuilderHand{

        String handPart;

        public BindingPathBuilderHand(String handPart){
            this.handPart = handPart;
        }

        public String menuClick(){
            return handPart + ComponentPaths.MENU_CLICK;
        }

        public String squeezeClick(){
            return handPart + ComponentPaths.SQUEEZE_CLICK;
        }

        public String triggerValue(){
            return handPart + ComponentPaths.TRIGGER_VALUE;
        }

        public String thumbStickX(){
            return handPart + ComponentPaths.THUMB_STICK_X;
        }

        public String thumbStickY(){
            return handPart + ComponentPaths.THUMB_STICK_Y;
        }

        public String thumbStickClick(){
            return handPart + ComponentPaths.THUMB_STICK_CLICK;
        }

        public String trackpadX(){
            return handPart + ComponentPaths.TRACKPAD_X;
        }

        public String trackpadY(){
            return handPart + ComponentPaths.TRACKPAD_Y;
        }

        public String trackpadClick(){
            return handPart + ComponentPaths.TRACKPAD_CLICK;
        }

        public String trackpadTouch(){
            return handPart + ComponentPaths.TRACKPAD_TOUCH;
        }

        public String gripPost(){
            return handPart + ComponentPaths.GRIP_POST;
        }

        public String aimPose(){
            return handPart + ComponentPaths.AIM_POSE;
        }

        public String haptic(){
            return handPart + ComponentPaths.HAPTIC;
        }
    }
}
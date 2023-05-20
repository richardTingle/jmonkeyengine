package com.jme3.actions.controllerprofile.oculus;

public class ValveIndexController {
    public static final String PROFILE = "/interaction_profiles/valve/index_controller";

    public static class InteractionProfiles{
        public static final String LEFT_HAND = "/user/hand/left";

        public static final String RIGHT_HAND = "/user/hand/right";
    }

    public static class ComponentPaths {
        /**
         * May not be available for application use
         */
        public static final String SYSTEM_CLICK = "/input/system/click";

        /**
         * May not be available for application use
         */
        public static final String SYSTEM_TOUCH = "/input/system/touch";

        public static final String A_CLICK = "/input/a/click";
        public static final String A_TOUCH = "/input/a/touch";

        public static final String B_CLICK = "/input/b/click";
        public static final String B_TOUCH = "/input/b/touch";

        public static final String SQUEEZE_VALUE = "/input/squeeze/value";
        public static final String SQUEEZE_FORCE = "/input/squeeze/force";

        public static final String TRIGGER_CLICK = "/input/trigger/click";
        public static final String TRIGGER_VALUE = "/input/trigger/value";
        public static final String TRIGGER_TOUCH = "/input/trigger/touch";

        public static final String THUMB_STICK_X = "/input/thumbstick/x";
        public static final String THUMB_STICK_Y = "/input/thumbstick/y";
        public static final String THUMB_STICK_CLICK = "/input/thumbstick/click";
        public static final String THUMB_STICK_TOUCH = "/input/thumbstick/touch";

        public static final String TRACKPAD_X = "/input/trackpad/x";
        public static final String TRACKPAD_Y = "/input/trackpad/y";
        public static final String TRACKPAD_FORCE = "/input/trackpad/force";
        public static final String TRACKPAD_TOUCH = "/input/trackpad/touch";

        public static final String GRIP_POSE = "/input/grip/pose";
        public static final String AIM_POSE = "/input/aim/pose";

        public static final String HAPTIC = "/output/haptic";
    }

    public static BindingPathBuilder pathBuilder(){
        return new BindingPathBuilder();
    }

    public static class BindingPathBuilder{
        public BindingPathBuilderHand leftHand(){
            return new BindingPathBuilderHand(InteractionProfiles.LEFT_HAND);
        }
        public BindingPathBuilderHand rightHand(){
            return new BindingPathBuilderHand(InteractionProfiles.RIGHT_HAND);
        }
    }

    public static class BindingPathBuilderHand {

        String handPart;

        public BindingPathBuilderHand(String handPart){
            this.handPart = handPart;
        }

        /**
         * May not be available for application use
         */
        public String systemClick(){
            return handPart + ComponentPaths.SYSTEM_CLICK;
        }

        /**
         * May not be available for application use
         */
        public String systemTouch(){
            return handPart + ComponentPaths.SYSTEM_TOUCH;
        }

        public String aClick(){
            return handPart + ComponentPaths.A_CLICK;
        }

        public String aTouch(){
            return handPart + ComponentPaths.A_TOUCH;
        }

        public String bClick(){
            return handPart + ComponentPaths.B_CLICK;
        }

        public String bTouch(){
            return handPart + ComponentPaths.B_TOUCH;
        }

        public String squeezeValue(){
            return handPart + ComponentPaths.SQUEEZE_VALUE;
        }

        public String squeezeForce(){
            return handPart + ComponentPaths.SQUEEZE_FORCE;
        }

        public String triggerClick(){
            return handPart + ComponentPaths.TRIGGER_CLICK;
        }

        public String triggerValue(){
            return handPart + ComponentPaths.TRIGGER_VALUE;
        }

        public String triggerTouch(){
            return handPart + ComponentPaths.TRIGGER_TOUCH;
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

        public String thumbStickTouch(){
            return handPart + ComponentPaths.THUMB_STICK_TOUCH;
        }

        public String trackpadX(){
            return handPart + ComponentPaths.TRACKPAD_X;
        }

        public String trackpadY(){
            return handPart + ComponentPaths.TRACKPAD_Y;
        }

        public String trackpadForce(){
            return handPart + ComponentPaths.TRACKPAD_FORCE;
        }

        public String trackpadTouch(){
            return handPart + ComponentPaths.TRACKPAD_TOUCH;
        }

        public String gripPose(){
            return handPart + ComponentPaths.GRIP_POSE;
        }

        public String aimPose(){
            return handPart + ComponentPaths.AIM_POSE;
        }

        public String haptic(){
            return handPart + ComponentPaths.HAPTIC;
        }
    }
}
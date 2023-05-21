package com.jme3.actions.controllerprofile;

public class XboxController {
    public static final String PROFILE = "/interaction_profiles/microsoft/xbox_controller";

    public static final String GAMEPAD = "/user/gamepad";

    public static class ComponentPaths {
        public static final String MENU_CLICK = "/input/menu/click";
        public static final String VIEW_CLICK = "/input/view/click";

        public static final String A_CLICK = "/input/a/click";
        public static final String B_CLICK = "/input/b/click";
        public static final String X_CLICK = "/input/x/click";
        public static final String Y_CLICK = "/input/y/click";

        public static final String DPAD_DOWN_CLICK = "/input/dpad_down/click";
        public static final String DPAD_RIGHT_CLICK = "/input/dpad_right/click";
        public static final String DPAD_UP_CLICK = "/input/dpad_up/click";
        public static final String DPAD_LEFT_CLICK = "/input/dpad_left/click";

        public static final String SHOULDER_LEFT_CLICK = "/input/shoulder_left/click";
        public static final String SHOULDER_RIGHT_CLICK = "/input/shoulder_right/click";

        public static final String THUMBSTICK_LEFT_CLICK = "/input/thumbstick_left/click";
        public static final String THUMBSTICK_RIGHT_CLICK = "/input/thumbstick_right/click";

        public static final String TRIGGER_LEFT_VALUE = "/input/trigger_left/value";
        public static final String TRIGGER_RIGHT_VALUE = "/input/trigger_right/value";

        public static final String THUMBSTICK_LEFT_X = "/input/thumbstick_left/x";
        public static final String THUMBSTICK_LEFT_Y = "/input/thumbstick_left/y";

        public static final String THUMBSTICK_RIGHT_X = "/input/thumbstick_right/x";
        public static final String THUMBSTICK_RIGHT_Y = "/input/thumbstick_right/y";

        public static final String HAPTIC_LEFT = "/output/haptic_left";
        public static final String HAPTIC_RIGHT = "/output/haptic_right";

        public static final String HAPTIC_LEFT_TRIGGER = "/output/haptic_left_trigger";
        public static final String HAPTIC_RIGHT_TRIGGER = "/output/haptic_right_trigger";
    }

    public static BindingPathBuilder pathBuilder(){
        return new BindingPathBuilder();
    }

    public static class BindingPathBuilder {

        String gamepadPart = GAMEPAD;

        public String menuClick(){
            return gamepadPart + ComponentPaths.MENU_CLICK;
        }

        public String viewClick(){
            return gamepadPart + ComponentPaths.VIEW_CLICK;
        }

        public String aClick(){
            return gamepadPart + ComponentPaths.A_CLICK;
        }

        public String bClick(){
            return gamepadPart + ComponentPaths.B_CLICK;
        }

        public String xClick(){
            return gamepadPart + ComponentPaths.X_CLICK;
        }

        public String yClick(){
            return gamepadPart + ComponentPaths.Y_CLICK;
        }

        public String dpadDownClick(){
            return gamepadPart + ComponentPaths.DPAD_DOWN_CLICK;
        }

        public String dpadRightClick(){
            return gamepadPart + ComponentPaths.DPAD_RIGHT_CLICK;
        }

        public String dpadUpClick(){
            return gamepadPart + ComponentPaths.DPAD_UP_CLICK;
        }

        public String dpadLeftClick(){
            return gamepadPart + ComponentPaths.DPAD_LEFT_CLICK;
        }

        public String shoulderLeftClick(){
            return gamepadPart + ComponentPaths.SHOULDER_LEFT_CLICK;
        }

        public String shoulderRightClick(){
            return gamepadPart + ComponentPaths.SHOULDER_RIGHT_CLICK;
        }

        public String thumbstickLeftClick(){
            return gamepadPart + ComponentPaths.THUMBSTICK_LEFT_CLICK;
        }

        public String thumbstickRightClick(){
            return gamepadPart + ComponentPaths.THUMBSTICK_RIGHT_CLICK;
        }

        public String triggerLeftValue(){
            return gamepadPart + ComponentPaths.TRIGGER_LEFT_VALUE;
        }

        public String triggerRightValue(){
            return gamepadPart + ComponentPaths.TRIGGER_RIGHT_VALUE;
        }

        public String thumbstickLeftX(){
            return gamepadPart + ComponentPaths.THUMBSTICK_LEFT_X;
        }

        public String thumbstickLeftY(){
            return gamepadPart + ComponentPaths.THUMBSTICK_LEFT_Y;
        }

        public String thumbstickRightX(){
            return gamepadPart + ComponentPaths.THUMBSTICK_RIGHT_X;
        }

        public String thumbstickRightY(){
            return gamepadPart + ComponentPaths.THUMBSTICK_RIGHT_Y;
        }

        public String hapticLeft(){
            return gamepadPart + ComponentPaths.HAPTIC_LEFT;
        }

        public String hapticRight(){
            return gamepadPart + ComponentPaths.HAPTIC_RIGHT;
        }

        public String hapticLeftTrigger(){
            return gamepadPart + ComponentPaths.HAPTIC_LEFT_TRIGGER;
        }

        public String hapticRightTrigger(){
            return gamepadPart + ComponentPaths.HAPTIC_RIGHT_TRIGGER;
        }
    }
}
package com.jme3.actions.actionprofile;

import com.jme3.actions.ActionType;
import com.jme3.actions.HandSide;
import com.jme3.actions.controllerprofile.HtcViveController;
import com.jme3.actions.controllerprofile.KhronosSimpleController;
import com.jme3.actions.controllerprofile.MixedRealityMotionController;
import com.jme3.actions.controllerprofile.OculusTouchController;
import com.jme3.actions.controllerprofile.ValveIndexController;

import java.util.ArrayList;
import java.util.List;

public class Action{

    /**
     * By default, all actions support the left and right hand sides.
     */
    public static List<String> DEFAULT_SUB_ACTIONS = new ArrayList<>();

    static{
        DEFAULT_SUB_ACTIONS.add(HandSide.LEFT.restrictToInputString);
        DEFAULT_SUB_ACTIONS.add(HandSide.RIGHT.restrictToInputString);
    }

    /**
     * The action name. This should be things like "teleport", not things like "X Click". The idea is that they are
     * abstract concept your application would like to support and they are bound to specific buttons based on the suggested
     * bindings (which may be changed by the user, or guessed at by the binding).
     */
    private final String actionName;

    /**
     * This is presented to the user as a description of the action. This string should be presented in the system’s current active locale.
     */
    private final String translatedName;

    private final ActionType actionType;
    /**
     * These are physical bindings to specific devices for this action.
     */
    private final List<SuggestedBinding> suggestedBindings;

    private final List<String> supportedSubActionPaths;

    public Action(String actionName, String translatedName, ActionType actionType, List<SuggestedBinding> suggestedBindings){
        this.actionName = actionName;
        this.translatedName = translatedName;
        this.actionType = actionType;
        this.suggestedBindings = suggestedBindings;
        this.supportedSubActionPaths = DEFAULT_SUB_ACTIONS;
    }

    public Action(String actionName, String translatedName, ActionType actionType, List<SuggestedBinding> suggestedBindings, List<String> supportedSubActionPaths){
        this.actionName = actionName;
        this.translatedName = translatedName;
        this.actionType = actionType;
        this.suggestedBindings = suggestedBindings;
        this.supportedSubActionPaths = supportedSubActionPaths;
    }

    public String getActionName(){
        return actionName;
    }

    public String getTranslatedName(){
        return translatedName;
    }

    public ActionType getActionType(){
        return actionType;
    }

    public List<SuggestedBinding> getSuggestedBindings(){
        return suggestedBindings;
    }

    public List<String> getSupportedSubActionPaths(){
        return supportedSubActionPaths;
    }

    public static ActionBuilder builder(){
        return new ActionBuilder();
    }

    public static class ActionBuilder{
        private String actionName;
        private String translatedName;

        private ActionType actionType;
        /**
         * These are physical bindings to specific devices for this action.
         */
        private final List<SuggestedBinding> suggestedBindings = new ArrayList<>();

        private List<String> supportedSubActionPaths = DEFAULT_SUB_ACTIONS;

        /**
         * The action name. This should be things like "teleport", not things like "X Click". The idea is that they are
         * abstract concept your application would like to support and they are bound to specific buttons based on the suggested
         * bindings (which may be changed by the user, or guessed at by the binding).
         */
        public ActionBuilder actionName(String actionName){
            this.actionName = actionName;
            return this;
        }

        /**
         * This is presented to the user as a description of the action. This string should be presented in the system’s current active locale.
         */
        public ActionBuilder translatedName(String translatedName){
            this.translatedName = translatedName;
            return this;
        }

        public ActionBuilder actionType(ActionType actionType){
            this.actionType = actionType;
            return this;
        }


        public ActionBuilder withSuggestedBinding(String profile, String binding){
            this.suggestedBindings.add(new SuggestedBinding(profile, binding));
            return this;
        }
        /**
         * Suggested bindings are physical bindings to specific devices for this action.
         * This method can be called multiple times to add more bindings.
         */
        public ActionBuilder withSuggestedBinding(SuggestedBinding suggestedBinding){
            this.suggestedBindings.add(suggestedBinding);
            return this;
        }

        /**
         * Binds all the devices this library knows about (and that have haptics) to this action for both hands.
         */
        public ActionBuilder withSuggestAllKnownHapticBindings(){
            withSuggestedBinding(OculusTouchController.PROFILE, OculusTouchController.pathBuilder().leftHand().haptic());
            withSuggestedBinding(OculusTouchController.PROFILE, OculusTouchController.pathBuilder().rightHand().haptic());
            withSuggestedBinding(HtcViveController.PROFILE, HtcViveController.pathBuilder().leftHand().haptic());
            withSuggestedBinding(HtcViveController.PROFILE, HtcViveController.pathBuilder().rightHand().haptic());
            withSuggestedBinding(KhronosSimpleController.PROFILE, KhronosSimpleController.pathBuilder().leftHand().haptic());
            withSuggestedBinding(KhronosSimpleController.PROFILE, KhronosSimpleController.pathBuilder().rightHand().haptic());
            withSuggestedBinding(MixedRealityMotionController.PROFILE, MixedRealityMotionController.pathBuilder().leftHand().haptic());
            withSuggestedBinding(MixedRealityMotionController.PROFILE, MixedRealityMotionController.pathBuilder().rightHand().haptic());
            withSuggestedBinding(ValveIndexController.PROFILE, ValveIndexController.pathBuilder().leftHand().haptic());
            withSuggestedBinding(ValveIndexController.PROFILE, ValveIndexController.pathBuilder().rightHand().haptic());
            return this;
        }

        /**
         * Sub action paths are things like "/user/hand/left", for use when restricting actions to a specific input source.
         * This is defaulted to ["/user/hand/left", "/user/hand/right"] and there is usually no reason to change it.
         */
        public void overrideSupportedSubActionPaths(List<String> supportedSubActionPaths){
            this.supportedSubActionPaths = supportedSubActionPaths;
        }

        public Action build(){
            if(actionName == null){
                throw new IllegalArgumentException("name cannot be null");
            }
            if(translatedName == null){
                throw new IllegalArgumentException("translatedName cannot be null");
            }
            if(actionType == null){
                throw new IllegalArgumentException("actionType cannot be null");
            }
            if(suggestedBindings.isEmpty()){
                throw new IllegalArgumentException("suggestedBindings cannot be empty");
            }
            return new Action(actionName, translatedName, actionType, suggestedBindings, supportedSubActionPaths);
        }

    }


}

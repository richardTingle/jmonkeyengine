package com.jme3.actions.actionprofile;

import com.jme3.actions.ActionType;

import java.util.ArrayList;
import java.util.List;

public class Action{

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

    public Action(String actionName, String translatedName, ActionType actionType, List<SuggestedBinding> suggestedBindings){
        this.actionName = actionName;
        this.translatedName = translatedName;
        this.actionType = actionType;
        this.suggestedBindings = suggestedBindings;
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

    public static class ActionBuilder{
        private String actionName;
        private String translatedName;

        private ActionType actionType;
        /**
         * These are physical bindings to specific devices for this action.
         */
        private final List<SuggestedBinding> suggestedBindings = new ArrayList<>();

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

        /**
         * Suggested bindings are physical bindings to specific devices for this action.
         * This method can be called multiple times to add more bindings.
         */
        public ActionBuilder withSuggestedBinding(SuggestedBinding suggestedBinding){
            this.suggestedBindings.add(suggestedBinding);
            return this;
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
            return new Action(actionName, translatedName, actionType, suggestedBindings);
        }
    }


}

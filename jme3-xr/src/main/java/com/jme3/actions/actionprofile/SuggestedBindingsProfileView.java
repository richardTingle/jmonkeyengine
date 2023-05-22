package com.jme3.actions.actionprofile;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a view of the suggested bindings where they are all together for a particular profile (device).
 * This is not intended to be created directly (as its defined action with bindings per device) but to provide a view
 * on bindings from a profile (device) first approach.
 * <p>
 * Primarily for internal use.
 */
public class SuggestedBindingsProfileView{

    String profileName;

    Map<String,String> actionToBindingMap = new HashMap<>();

    SuggestedBindingsProfileView(String profileName){
        this.profileName = profileName;
    }

    void addSuggestion(String action, String binding){
        actionToBindingMap.put(action, binding);
    }

    public String getProfileName(){
        return profileName;
    }

    public Map<String, String> getActionToBindingMap(){
        return actionToBindingMap;
    }


}

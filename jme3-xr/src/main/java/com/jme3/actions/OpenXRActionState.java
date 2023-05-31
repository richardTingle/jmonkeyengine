package com.jme3.actions;

import com.jme3.actions.actionprofile.Action;
import com.jme3.actions.actionprofile.ActionManifest;
import com.jme3.actions.actionprofile.ActionSet;
import com.jme3.actions.actionprofile.SuggestedBindingsProfileView;
import com.jme3.actions.state.BooleanActionState;
import com.jme3.actions.state.FloatActionState;
import com.jme3.app.Application;

import com.jme3.app.state.BaseAppState;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;

import org.lwjgl.openxr.XR10;
import org.lwjgl.openxr.XrAction;
import org.lwjgl.openxr.XrActionCreateInfo;
import org.lwjgl.openxr.XrActionSet;
import org.lwjgl.openxr.XrActionSetCreateInfo;
import org.lwjgl.openxr.XrActionStateBoolean;
import org.lwjgl.openxr.XrActionStateFloat;
import org.lwjgl.openxr.XrActionStateGetInfo;
import org.lwjgl.openxr.XrActionSuggestedBinding;
import org.lwjgl.openxr.XrActionsSyncInfo;
import org.lwjgl.openxr.XrActiveActionSet;
import org.lwjgl.openxr.XrHapticActionInfo;
import org.lwjgl.openxr.XrHapticBaseHeader;
import org.lwjgl.openxr.XrHapticVibration;
import org.lwjgl.openxr.XrInstance;
import org.lwjgl.openxr.XrInteractionProfileSuggestedBinding;
import org.lwjgl.openxr.XrSession;
import org.lwjgl.openxr.XrSessionActionSetsAttachInfo;
import org.lwjgl.system.MemoryUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This app state provides action based OpenXR calls (aka modern VR and AR).
 * <p>
 * See <a href="https://registry.khronos.org/OpenXR/specs/1.0/html/xrspec.html#_action_overview">khronos spec action_overview</a>
 */
public class OpenXRActionState extends BaseAppState{

    boolean suppressRepeatedErrors = true;

    Set<String> errorsPreviouslyReported = new HashSet<>();

    public static final String ID = "ActionBasedOpenXRState";

    private static final Quaternion HALF_ROTATION_ABOUT_Y = new Quaternion();

    private static final Logger logger = Logger.getLogger(OpenXRActionState.class.getName());

    /**
     * A map of the action name to the objects/data required to read states from lwjgl
     */
    //private final Map<String, LWJGLOpenVRDigitalActionData> digitalActions = new HashMap<>();

    /**
     * A map of the action name to the objects/data required to read states from lwjgl
     */
    //private final Map<String, LWJGLOpenVRAnalogActionData> analogActions = new HashMap<>();

    /**
     * A map of the action name to the handle of a haptic action
     */
    private final Map<String, Long> hapticActionHandles = new HashMap<>();

    /**
     * A map of the action set name to the handle that is used to refer to it when talking to LWJGL
     */
    private final Map<String, Long> actionSetHandles = new HashMap<>();

    /**
     * These are the cached skeleton data (what bones there are, what the handles are etc)
     * <p>
     * It is a map of action name to that name (/skeleton/hand/left or /skeleton/hand/right should be bound to an
     * action of type skeleton in the action manifest).
     */
    //private final Map<String, LWJGLSkeletonData> skeletonActions = new HashMap<>();

    /**
     * A map of paths (e.g. /user/hand/right) to the handle used to address it.
     */
    private final Map<String, Long> pathCache = new HashMap<>();

    private final XrSession xrSession;
    private final XrInstance xrInstance;

    private Map<String, XrActionSet> actionSets;

    /**
     * This is action set -> action name -> action
     */
    private Map<String, Map<String,XrAction>> actions;

    /**
     * Contains the currently active profiles
     */
    private XrActionsSyncInfo xrActionsSyncInfo;

    static {
        HALF_ROTATION_ABOUT_Y.fromAngleAxis(FastMath.PI, Vector3f.UNIT_Y);
    }

    public OpenXRActionState(XrSession xrSession, XrInstance xrInstance){
        super(ID);
        this.xrSession = xrSession;
        this.xrInstance = xrInstance;
    }

    @Override
    public void initialize(Application app){

    }

    @Override
    protected void cleanup(Application app){

    }

    @Override
    protected void onEnable(){

    }

    @Override
    protected void onDisable(){

    }



    /**
     * Registers an action manifest. An actions manifest is a file that defines "actions" a player can make.
     * (An action is an abstract version of a button press). The action manifest may then also include references to
     * further files that define default mappings between those actions and physical buttons on the VR controllers.
     * <p>
     * @param manifest a class describing all the actions (abstract versions of buttons, hand poses etc) available to the application
     * @param startingActionSets the names of the action sets that should be activated at the start of the application (aka the ones that will work)
     */
    public void registerActions(ActionManifest manifest, String... startingActionSets){
        //see https://registry.khronos.org/OpenXR/specs/1.0/html/xrspec.html#_action_overview for examples of these calls

        assert actionSets == null: "Actions have already been registered, consider using action sets and activating and deactivating them as required";

        if (startingActionSets.length == 0){
            logger.log(Level.WARNING, "No starting action sets specified, that means no actions will be usable. Probably not what you want.");
        }

        actionSets = new HashMap<>();
        actions = new HashMap<>();

        Map<List<String>, LongBuffer> subActionPaths = new HashMap<>();

        for(ActionSet actionSet : manifest.getActionSets()){
            XrActionSetCreateInfo actionSetCreate = XrActionSetCreateInfo.create();
            actionSetCreate.actionSetName(stringToByte(actionSet.getName()));
            actionSetCreate.localizedActionSetName(stringToByte(actionSet.getTranslatedName()));
            actionSetCreate.priority(actionSet.getPriority());

            PointerBuffer actionSetPointer = BufferUtils.createPointerBuffer(1);
            withResponseCodeLogging("Create action set", XR10.xrCreateActionSet(xrInstance, actionSetCreate, actionSetPointer));
            actionSetCreate.close();

            XrActionSet xrActionSet = new XrActionSet(actionSetPointer.get(), xrInstance);
            actionSets.put(actionSet.getName(), xrActionSet);

            for(Action action : actionSet.getActions()){
                XrActionCreateInfo xrActionCreateInfo = XrActionCreateInfo.create();
                xrActionCreateInfo.actionName(stringToByte(action.getActionName()));
                xrActionCreateInfo.actionType(action.getActionType().getOpenXrOption());
                xrActionCreateInfo.localizedActionName(stringToByte(action.getTranslatedName()));

                List<String> supportedSubActionPaths = action.getSupportedSubActionPaths();
                if (!action.getSupportedSubActionPaths().isEmpty()){
                    LongBuffer subActionsLongBuffer = subActionPaths.computeIfAbsent(supportedSubActionPaths, paths -> {
                        LongBuffer standardSubActionPaths = BufferUtils.createLongBuffer(paths.size());
                        for(String path : paths){
                            standardSubActionPaths.put(pathToLong(path,true));
                        }
                        return standardSubActionPaths;
                    });
                    subActionsLongBuffer.rewind();
                    xrActionCreateInfo.subactionPaths(subActionsLongBuffer);
                }

                PointerBuffer actionPointer = BufferUtils.createPointerBuffer(1);
                withResponseCodeLogging("xrStringToPath", XR10.xrCreateAction(xrActionSet, xrActionCreateInfo, actionPointer));
                XrAction xrAction = new XrAction(actionPointer.get(), xrActionSet);
                actions.computeIfAbsent(actionSet.getName(), name -> new HashMap<>()).put(action.getActionName(), xrAction);

                xrActionCreateInfo.close();
            }
        }

        Collection<SuggestedBindingsProfileView> suggestedBindingsGroupedByProfile = manifest.getSuggestedBindingsGroupedByProfile();

        for(SuggestedBindingsProfileView profile : suggestedBindingsGroupedByProfile){
            long deviceProfileHandle = pathToLong(profile.getProfileName(), false);

            Set<Map.Entry<SuggestedBindingsProfileView.ActionData, String>> suggestedBindings = profile.getSetToActionToBindingMap().entrySet();
            XrActionSuggestedBinding.Buffer suggestedBindingsBuffer = XrActionSuggestedBinding.create(suggestedBindings.size());

            Iterator<Map.Entry<SuggestedBindingsProfileView.ActionData, String>> suggestedBindingIterator = suggestedBindings.iterator();
            for(int i=0; i<suggestedBindings.size(); i++){
                Map.Entry<SuggestedBindingsProfileView.ActionData, String> actionAndBinding = suggestedBindingIterator.next();
                LongBuffer bindingHandleBuffer = BufferUtils.createLongBuffer(1);
                withResponseCodeLogging("xrStringToPath:" + actionAndBinding.getValue(),XR10.xrStringToPath(xrInstance, actionAndBinding.getValue(), bindingHandleBuffer));

                XrAction action = actions.get(actionAndBinding.getKey().getActionSet()).get(actionAndBinding.getKey().getActionName());
                suggestedBindingsBuffer.position(i);
                suggestedBindingsBuffer.action(action);
                suggestedBindingsBuffer.binding(bindingHandleBuffer.get());
            }
            suggestedBindingsBuffer.position(0); //reset ready for reading

            XrInteractionProfileSuggestedBinding xrInteractionProfileSuggestedBinding = XrInteractionProfileSuggestedBinding.create()
                    .type(XR10.XR_TYPE_INTERACTION_PROFILE_SUGGESTED_BINDING)
                    .interactionProfile(deviceProfileHandle)
                    .suggestedBindings(suggestedBindingsBuffer);

            withResponseCodeLogging("xrSuggestInteractionProfileBindings", XR10.xrSuggestInteractionProfileBindings(xrInstance, xrInteractionProfileSuggestedBinding));
        }

        PointerBuffer actionSetsBuffer = BufferUtils.createPointerBuffer(actionSets.size());

        actionSets.values().forEach(actionSet -> actionSetsBuffer.put(actionSet.address()));
        actionSetsBuffer.flip();  // Reset the position back to the start of the buffer

        XrSessionActionSetsAttachInfo actionSetsAttachInfo = XrSessionActionSetsAttachInfo.create();
        actionSetsAttachInfo.type(XR10.XR_TYPE_SESSION_ACTION_SETS_ATTACH_INFO);
        actionSetsAttachInfo.actionSets(actionSetsBuffer);
        withResponseCodeLogging("xrAttachSessionActionSets", XR10.xrAttachSessionActionSets(xrSession, actionSetsAttachInfo));

        setActiveActionSets(startingActionSets);
    }

    /**
     * This sets the action sets that will be active. I.e. the actions in this action set will work, others will be ignored
     * @param actionSets the names of the action sets
     */
    public void setActiveActionSets(String... actionSets){

        List<XrActionSet> activeActionSets = new ArrayList<>(actionSets.length);

        for(String actionSet : actionSets){
            XrActionSet actionSetXr = this.actionSets.get(actionSet);
            if(actionSetXr==null){
                throw new RuntimeException("Action set not found: " + actionSet);
            }
            activeActionSets.add(actionSetXr);
        }

        this.xrActionsSyncInfo = XrActionsSyncInfo.create();
        this.xrActionsSyncInfo.countActiveActionSets(activeActionSets.size());
        XrActiveActionSet.Buffer activeActionSetsBuffer = XrActiveActionSet.calloc(activeActionSets.size());
        for(XrActionSet activeActionSet : activeActionSets){
            activeActionSetsBuffer.actionSet(activeActionSet);
        }
        for(int i=0; i<activeActionSets.size(); i++){
            activeActionSetsBuffer.position(i);
            activeActionSetsBuffer.actionSet(activeActionSets.get(i));
        }
        activeActionSetsBuffer.position(0);
        this.xrActionsSyncInfo.activeActionSets(activeActionSetsBuffer);
    }

    private static ByteBuffer stringToByte(String str){
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
        byte[] nullTerminatedBytes = new byte[strBytes.length + 1];
        System.arraycopy(strBytes, 0, nullTerminatedBytes, 0, strBytes.length);
        nullTerminatedBytes[nullTerminatedBytes.length - 1] = 0;  // add null terminator
        ByteBuffer buffer = BufferUtils.createByteBuffer(nullTerminatedBytes.length);
        buffer.put(nullTerminatedBytes);
        buffer.rewind();
        return buffer;
    }

    private void withResponseCodeLogging(String eventText, int errorCode){
        //error code 0 is ultra common and means all is well. Don't flood the logs with it
        if (errorCode != XR10.XR_SUCCESS){
            ByteBuffer buffer = BufferUtils.createByteBuffer(XR10.XR_MAX_RESULT_STRING_SIZE);
            XR10.xrResultToString(xrInstance, errorCode, buffer);

            String message = errorCode + " " + MemoryUtil.memUTF8(buffer, MemoryUtil.memLengthNT1(buffer))+ " occurred during " + eventText+ ". ";

            if (errorCode<0){

                if (!suppressRepeatedErrors || !errorsPreviouslyReported.contains(message)){
                    errorsPreviouslyReported.add(message);

                    message += CallResponseCode.getResponseCode(errorCode).map(CallResponseCode::getErrorMessage).orElse("");

                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    new Throwable(message).printStackTrace(pw);
                    logger.warning(sw.toString());

                    if (suppressRepeatedErrors){
                        logger.warning("Further identical errors will be suppressed. If you don't want that call doNotSupressRepeatedErrors()");
                    }
                }
            }else{
                if (logger.isLoggable(Level.INFO)){
                    logger.info(message+CallResponseCode.getResponseCode(errorCode).map(CallResponseCode::getErrorMessage).orElse(""));
                }
            }
        }
    }

    public void doNotSuppressRepeatedErrors(){
        suppressRepeatedErrors = false;
    }

    /**
     * Gets the current state of the action (abstract version of a button press).
     * <p>
     * This is called for digital style actions (a button is pressed, or not)
     * <p>
     * {@link #registerActions} must have been called before using this method.
     *
     * @param actionSet The name of the action set. E.g. inGameActions
     * @param actionName The name of the action. E.g. openInventory
     * @return the DigitalActionState that has details on if the state has changed, what the state is etc.
     */
    public BooleanActionState getBooleanActionState(String actionSet, String actionName){
        return getBooleanActionState(obtainActionHandle(actionSet, actionName), null);
    }

    /**
     * Gets the current state of the action (abstract version of a button press).
     * <p>
     * This is called for digital style actions (a button is pressed, or not)
     * <p>
     * {@link #registerActions} must have been called before using this method.
     *
     * @param actionSet The name of the action set. E.g. inGameActions
     * @param actionName The name of the action. E.g. openInventory
     * @param restrictToInput If the same action is bound to multiple hands then restrict to hand can be used to
     *                        only return the value from one hand. E.g. "/user/hand/left". See {@link HandSide} which
     *                        contains common values. Other (probably less useful) known values are: "/user/gamepad"
     *                        and "/user/head". Can be null for no restriction
     * @return the DigitalActionState that has details on if the state has changed, what the state is etc.
     */
    public BooleanActionState getBooleanActionState(String actionSet, String actionName, String restrictToInput){
        return getBooleanActionState(obtainActionHandle(actionSet, actionName), restrictToInput);
    }

    /**
     * Gets the current state of the action (abstract version of a button press).
     * <p>
     * This is called for digital style actions (a button is pressed, or not)
     * <p>
     * {@link #registerActions} must have been called before using this method.
     *
     * @param action The action. E.g. openInventory
     * @param restrictToInput If the same action is bound to multiple hands then restrict to hand can be used to
     *                        only return the value from one hand. E.g. "/user/hand/left". See {@link HandSide} which
     *                        contains common values. Other (probably less useful) known values are: "/user/gamepad"
     *                        and "/user/head". Can be null for no restriction
     * @return the DigitalActionState that has details on if the state has changed, what the state is etc.
     */
    public BooleanActionState getBooleanActionState(XrAction action, String restrictToInput){
        XrActionStateBoolean actionState = XrActionStateBoolean.create();
        XrActionStateGetInfo actionInfo = XrActionStateGetInfo.create();
        actionInfo.action(action);

        if (restrictToInput != null){
            actionInfo.subactionPath(pathToLong(restrictToInput, true));
        }

        withResponseCodeLogging("getActionState", XR10.xrGetActionStateBoolean(xrSession, actionInfo, actionState));
        return new BooleanActionState(actionState.currentState(), actionState.changedSinceLastSync());
    }

    /**
     * This allows the XrAction object to be obtained for a particular action set and action name.
     * This handle can be used instead of quoting the name and set every time (it's also marginally faster
     * to hold onto these XrAction and use them directly rather than having {@link OpenXRActionState} look them up)
     */
    public XrAction obtainActionHandle(String actionSet, String actionName){
        return actions.get(actionSet).get(actionName);
    }

    /*
    public Vector3f getObserverPosition(){
        Object obs = environment.getObserver();
        if (obs instanceof Camera){
            Camera camera = ((Camera) obs);
            return camera.getLocation();
        }else{
            Spatial spatial = (Spatial)obs;
            return spatial.getWorldTranslation();
        }
    }
     */
    /*
    public Quaternion getObserverRotation(){
        Object obs = environment.getObserver();
        if (obs instanceof Camera){
            Camera camera = ((Camera) obs);
            return camera.getRotation();
        }else{
            Spatial spatial = (Spatial)obs;
            return spatial.getWorldRotation();
        }
    }
     */
    /**
     * A pose is where a hand is, and what its rotation is.
     * <p>
     * This returns the pose in the observers coordinate system (note that observer does not mean "eyes", it means
     * a reference point placed in the scene that corresponds to the real world VR origin)
     *
     * @param actionName the action name that has been bound to a pose in the action manifest
     * @return the PoseActionState
     */
    /*
    public ObserverRelativePoseActionState getPose_observerRelative(String actionName){
        PoseActionState worldRelative = getPose(actionName);

        Vector3f observerPosition = getObserverPosition();
        Quaternion observerRotation = getObserverRotation();

        Node calculationNode = new Node();
        calculationNode.setLocalRotation(observerRotation);
        Vector3f velocity_observerRelative = calculationNode.worldToLocal(worldRelative.getVelocity(), null);
        Vector3f angularVelocity_observerRelative = calculationNode.worldToLocal(worldRelative.getAngularVelocity(), null);
        calculationNode.setLocalTranslation(observerPosition);

        Vector3f localPosition = calculationNode.worldToLocal(worldRelative.getPosition(), null);

        Quaternion localRotation = observerRotation.inverse().mult(worldRelative.getOrientation());

        return new ObserverRelativePoseActionState(worldRelative.getRawPose(), localPosition, localRotation, velocity_observerRelative, angularVelocity_observerRelative, worldRelative );
    }
    */

    /**
     * A pose is where a hand is, and what its rotation is.
     * <p>
     * Pose means the bulk position and rotation of the hand. Be aware that the direction the hand is pointing by this
     * may be surprising, the relative bone positions also need to be taken into account for this to really make sense.
     * <p>
     * This returns the pose in world coordinate system
     *
     * @param actionName the action name that has been bound to a pose in the action manifest
     * @return the PoseActionState
     */
    /*
    public PoseActionState getPose(String actionName){

        InputPoseActionData inputPose = InputPoseActionData.create();

        VRInput.VRInput_GetPoseActionDataForNextFrame(fetchActionHandle(actionName), environment.isSeatedExperience() ? VR.ETrackingUniverseOrigin_TrackingUniverseSeated : VR.ETrackingUniverseOrigin_TrackingUniverseStanding, inputPose, getOrFetchInputHandle(null));

        HmdMatrix34 hmdMatrix34 = inputPose.pose().mDeviceToAbsoluteTracking();

        Matrix4f pose = LWJGLOpenVR.convertSteamVRMatrix3ToMatrix4f(hmdMatrix34, new Matrix4f() );

        HmdVector3 velocityHmd = inputPose.pose().vVelocity();
        Vector3f velocity = new Vector3f(velocityHmd.v(0), velocityHmd.v(1), velocityHmd.v(2));
        HmdVector3 angularVelocityHmd =inputPose.pose().vAngularVelocity();
        Vector3f angularVelocity = new Vector3f(angularVelocityHmd.v(0), angularVelocityHmd.v(1), angularVelocityHmd.v(2));
        Vector3f position = pose.toTranslationVector();
        Quaternion rotation = pose.toRotationQuat();

        Vector3f observerPosition = getObserverPosition();
        Quaternion observerRotation = getObserverRotation();

        Node calculationNode = new Node();
        //the openVR and JMonkey define "not rotated" to be a different rotation, the HALF_ROTATION_ABOUT_Y corrects that
        calculationNode.setLocalRotation(HALF_ROTATION_ABOUT_Y.mult(observerRotation));

        Vector3f worldRelativeVelocity =  calculationNode.localToWorld(velocity, null);
        Vector3f worldRelativeAngularVelocity = calculationNode.localToWorld(angularVelocity, null);

        calculationNode.setLocalTranslation(observerPosition);

        Vector3f worldRelativePosition = calculationNode.localToWorld(position, null);
        Quaternion worldRelativeRotation = HALF_ROTATION_ABOUT_Y.mult(observerRotation).mult(rotation);

        //the velocity and rotational velocity are in the wrong coordinate systems. This is wrong and a bug
        return new PoseActionState(pose, worldRelativePosition, worldRelativeRotation, worldRelativeVelocity, worldRelativeAngularVelocity);
    }
    */

    /**
     * Gets the current state of the action (abstract version of a button press).
     * <p>
     * This is called for analog style actions (most commonly triggers, but button pressure can also be mapped in analog).
     * <p>
     * This method is commonly called when it's not important which hand the action is bound to (e.g. if the thumb stick
     * is controlling a third-person character in-game that could be bound to either left or right hand and that would
     * not matter, or if the action will only be bound to one hand anyway).
     * <p>
     * If the handedness matters use {@link #getFloatActionState(String, String, String)}
     *
     * {@link #registerActions} must have been called before using this method.
     *
     * @param actionName The name of the action. E.g. /actions/main/in/openInventory
     * @return the AnalogActionState that has details on how much the state has changed, what the state is etc.
     */
    public FloatActionState getFloatActionState(String actionSet, String actionName ){
        return getFloatActionState(actionName, actionName, null);
    }

    /**
     * Gets the current state of the action (abstract version of a button press).
     * <p>
     * This is called for analog style actions (most commonly triggers but button pressure can also be mapped in analog).
     * <p>
     * This method is commonly called when it is important which hand the action is found on. For example an "in universe"
     * joystick that has a hat control might (while you are holding it) bind to the on-controller hat, but only on the hand
     * holding it
     * <p>
     * Note that restrictToInput only restricts, it must still be bound to the input you want to receive the input from
     * <p>
     * {@link #registerActions} must have been called before using this method.
     *
     * @param actionSet The action set.
     * @param actionName The action name.
     * @param restrictToInput the input to restrict the action to. E.g. /user/hand/right. Or null, which means "any input"
     * @return the AnalogActionState that has details on how much the state has changed, what the state is etc.
     */
    public FloatActionState getFloatActionState(String actionSet, String actionName, String restrictToInput ){
        return getFloatActionState(obtainActionHandle(actionSet, actionName), restrictToInput);
    }

    /**
     * Gets the current state of the action (abstract version of a button press).
     * <p>
     * This is called for analog style actions (most commonly triggers but button pressure can also be mapped in analog).
     * <p>
     * This method is commonly called when it is important which hand the action is found on. For example an "in universe"
     * joystick that has a hat control might (while you are holding it) bind to the on-controller hat, but only on the hand
     * holding it
     * <p>
     * Note that restrictToInput only restricts, it must still be bound to the input you want to receive the input from
     * <p>
     * {@link #registerActions} must have been called before using this method.
     *
     * @param action The action.
     * @param restrictToInput the input to restrict the action to. E.g. /user/hand/right. Or null, which means "any input"
     * @return the AnalogActionState that has details on how much the state has changed, what the state is etc.
     */
    public FloatActionState getFloatActionState(XrAction action, String restrictToInput ){

        XrActionStateFloat actionState = XrActionStateFloat.create();
        XrActionStateGetInfo actionInfo = XrActionStateGetInfo.create();
        actionInfo.action(action);

        if (restrictToInput != null){
            actionInfo.subactionPath(pathToLong(restrictToInput, true));
        }

        withResponseCodeLogging("getActionState", XR10.xrGetActionStateFloat(xrSession, actionInfo, actionState));
        return new FloatActionState(actionState.currentState(), actionState.changedSinceLastSync());
    }


    /**
     * Triggers a haptic action (aka a vibration).
     * <p>
     * Note if you want a haptic action in only one hand that is done either by only binding the action to one hand in
     * the action manifest's standard bindings or by binding to both and using {@link #triggerHapticAction(String, String, float, float, float, String)}
     * to control which input it gets set to at run time
     *
     * @param actionSet The name of the action Set. Will be something like main or an area of your application.
     * @param actionName The name of the action. Will be something like vibrate
     * @param duration how long in seconds the
     * @param frequency in cycles per second
     * @param amplitude between 0 and 1
     */
    public void triggerHapticAction(String actionSet, String actionName, float duration, float frequency, float amplitude){
        triggerHapticAction( actionSet, actionName, duration, frequency, amplitude, null );
    }

    /**
     * Triggers a haptic action (aka a vibration) restricted to just one input (e.g. left or right hand).
     * <p>
     * Note that restrictToInput only restricts, it must still be bound to the input you want to send the haptic to in
     * the action manifest default bindings.
     * <p>
     * This method is typically used to bind the haptic to both hands then decide at run time which hand to send to
     *
     * @param actionSet The name of the action Set. Will be something like main or an area of your application.
     * @param actionName The name of the action. Will be something like vibrate
     * @param duration how long in seconds the
     * @param frequency in cycles per second
     * @param amplitude between 0 and 1
     * @param restrictToInput the input to restrict the action to. E.g. /user/hand/right, /user/hand/left. Or null, which means "both hands"
     */
    public void triggerHapticAction(String actionSet, String actionName, float duration, float frequency, float amplitude, String restrictToInput){
        triggerHapticAction( obtainActionHandle(actionSet,actionName), duration, frequency, amplitude, null );
    }

    /**
     * Triggers a haptic action (aka a vibration) restricted to just one input (e.g. left or right hand).
     * <p>
     * Note that restrictToInput only restricts, it must still be bound to the input you want to send the haptic to in
     * the action manifest default bindings.
     * <p>
     * This method is typically used to bind the haptic to both hands then decide at run time which hand to sent to     *
     *
     * @param action The action for haptic vibration.
     * @param duration how long in seconds the
     * @param frequency in cycles per second (aka Hz)
     * @param amplitude between 0 and 1
     * @param restrictToInput the input to restrict the action to. E.g. /user/hand/right, /user/hand/left. Or null, which means "both hands"
     */
    public void triggerHapticAction(XrAction action, float duration, float frequency, float amplitude, String restrictToInput ){
        XrHapticVibration vibration = XrHapticVibration.create()
                .type(XR10.XR_TYPE_HAPTIC_VIBRATION)
                .duration((long)(duration * 1_000_000_000))  // Duration in nanoseconds
                .frequency(frequency)
                .amplitude(amplitude);  // Amplitude in normalized units

        XrHapticActionInfo hapticActionInfo = XrHapticActionInfo.malloc()
                .type(XR10.XR_TYPE_HAPTIC_ACTION_INFO)
                .action(action);

        if (restrictToInput!=null){
            hapticActionInfo.subactionPath(pathToLong(restrictToInput, true));
        }
        XrHapticBaseHeader hapticBaseHeader = XrHapticBaseHeader.create(vibration);

        withResponseCodeLogging("Haptic Vibration", XR10.xrApplyHapticFeedback(xrSession, hapticActionInfo, hapticBaseHeader));
    }

    @Override
    public void update(float tpf){
        super.update(tpf);
        if (xrActionsSyncInfo !=null){
            withResponseCodeLogging("xrSyncActions", XR10.xrSyncActions(xrSession, this.xrActionsSyncInfo));
        }
    }


    /*
    public Map<String, BoneStance> getModelRelativeSkeletonPositions(String actionName){
        LWJGLSkeletonData skeletonData = getOrFetchSkeletonData(actionName);

        ByteBuffer skeletonBuffer = BufferUtils.createByteBuffer(VRBoneTransform.SIZEOF*skeletonData.boneNames.length);
        VRBoneTransform.Buffer boneBuffer = new VRBoneTransform.Buffer(skeletonBuffer);

        VRInput.VRInput_GetSkeletalBoneData(skeletonData.skeletonAction, VR.EVRSkeletalTransformSpace_VRSkeletalTransformSpace_Model, VR.EVRSkeletalMotionRange_VRSkeletalMotionRange_WithoutController, boneBuffer);

        Map<String, BoneStance> positions = new HashMap<>();

        int i=0;
        for(VRBoneTransform boneTransform : boneBuffer){
            String boneName = skeletonData.boneNames[i];

            Vector3f position = new Vector3f(boneTransform.position$().v(0),boneTransform.position$().v(1), boneTransform.position$().v(2) );

            //flip the pitch
            //Quaternion rotation = new Quaternion(-boneTransform.orientation().x(), boneTransform.orientation().y(), -boneTransform.orientation().z(), boneTransform.orientation().w() );
            Quaternion rotation = new Quaternion(boneTransform.orientation().x(), boneTransform.orientation().y(), boneTransform.orientation().z(), boneTransform.orientation().w() );

            positions.put(boneName, new BoneStance(position, rotation) );
            i++;
        }
        return positions;
    }
    */
    /**
     * Given a hand armature (which should have 31 bones with names as defined in the below link)
     * it will pull from the requested action name and update the bones to be at the
     * appropriate positions. Note that all OpenVr compatible devices will have the same bone names
     * (although the fidelity of their positions may vary)
     * <p>
     * See <a href="https://github.com/ValveSoftware/openvr/wiki/Hand-Skeleton">Hand-Skeleton</a>
     * <p>
     * NOTE: the bone orientation is surprising and non-natural. If you build a hand model, and it appears
     * distorted try importing the example (as described in the above link) into blender from the fbx format. This will
     * give bones that appear not to lie along the anatomical bone set. Despite looking odd those bones work correctly
     * (and bones that track anatomical bones seemingly do not). The library Tamarin also has a starting blender file
     * that can be used.
     *
     * @param actionName the action name by which a particular skeleton has been bound to.
     * @param armature a JMonkey armature (aka set of bones)
     * @param handMode the hands "stance". See {@link HandMode} for more details
     */
    /*
    public void updateHandSkeletonPositions( String actionName, Armature armature, HandMode handMode ){
        LWJGLSkeletonData skeletonData = getOrFetchSkeletonData(actionName);

        ByteBuffer skeletonBuffer = BufferUtils.createByteBuffer(VRBoneTransform.SIZEOF*skeletonData.boneNames.length);
        VRBoneTransform.Buffer boneBuffer = new VRBoneTransform.Buffer(skeletonBuffer);

        //EVRSkeletalTransformSpace_VRSkeletalTransformSpace_Parent means ask for bones relative to their parent (which is also what JME armature wants to talk about)
        withResponseCodeLogging("Getting bone data", VRInput.VRInput_GetSkeletalBoneData(skeletonData.skeletonAction, VR.EVRSkeletalTransformSpace_VRSkeletalTransformSpace_Parent, handMode.openVrHandle, boneBuffer));

        int i=0;
        for(VRBoneTransform boneTransform : boneBuffer){
            String boneName = skeletonData.boneNames[i];

            HmdQuaternionf orientation = boneTransform.orientation();
            HmdVector4 position = boneTransform.position$();

            Vector3f positionJme = new Vector3f(position.v(0), position.v(1), position.v(2) );
            Quaternion orientationJME = new Quaternion(orientation.x(), orientation.y(),orientation.z() , orientation.w());

            Joint joint;
            if (boneName.equals("Root")){
                joint = armature.getRoots()[0];
            }else{
                joint = armature.getJoint(boneName);
            }
            if (joint!=null){
                joint.setLocalTranslation(positionJme);

                joint.setLocalRotation(orientationJME);
            }
            i++;
        }
    }
    */

    /**
     * Fetches (or gets from the cache) data on the hand skeleton
     *
     * //@param actionName the input name, e.g. /actions/default/in/HandSkeletonLeft.
     * @return data on the skeleton (e.g. names)
     */
    /*
    public LWJGLSkeletonData getOrFetchSkeletonData(String actionName ){
        if(!skeletonActions.containsKey(actionName)){

            long actionHandle = fetchActionHandle(actionName);

            IntBuffer intBuffer = BufferUtils.createIntBuffer(1);

            withResponseCodeLogging("getting Bone count", VRInput.VRInput_GetBoneCount(actionHandle, intBuffer) );

            int numberOfBones = intBuffer.get(0); //hopefully 31 for the full hand

            String[] boneNames = new String[numberOfBones];

            ByteBuffer byteBuffer = BufferUtils.createByteBuffer(200);

            for(int i=0;i<boneNames.length;i++){
                withResponseCodeLogging("getting Bone Name", VRInput.VRInput_GetBoneName(actionHandle, i, byteBuffer));

                byte[] bytes = new byte[byteBuffer.remaining()];
                byteBuffer.get(bytes);
                boneNames[i] = new String(bytes, StandardCharsets.UTF_8).trim();
                byteBuffer.rewind();
            }

            LWJGLSkeletonData skeletonData = new LWJGLSkeletonData(actionName, actionHandle, boneNames);
            if (numberOfBones>0){ //don't cache a failed attempt to get bones, maybe the controller will wake up later
                skeletonActions.put(actionName, skeletonData);
            }
            return skeletonData;
        }else{
            return skeletonActions.get(actionName);
        }
    }
    */

    private long pathToLong(String path, boolean cache){
        if (cache){
            Long handle = pathCache.get(path);
            if (handle!= null){
                return handle;
            }
        }

        LongBuffer pathHandleBuffer = BufferUtils.createLongBuffer(1);
        withResponseCodeLogging("xrStringToPath:"+path, XR10.xrStringToPath(xrInstance, path, pathHandleBuffer));
        long pathHandle = pathHandleBuffer.get(0);
        if (cache){
            pathCache.put(path, pathHandle);
        }
        return pathHandle;
    }

}

package com.jme3.actions;

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
import org.lwjgl.openxr.XrActionStateGetInfo;
import org.lwjgl.openxr.XrActionSuggestedBinding;
import org.lwjgl.openxr.XrActionsSyncInfo;
import org.lwjgl.openxr.XrActiveActionSet;
import org.lwjgl.openxr.XrInstance;
import org.lwjgl.openxr.XrInteractionProfileSuggestedBinding;
import org.lwjgl.openxr.XrSession;
import org.lwjgl.openxr.XrSessionActionSetsAttachInfo;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * This app state provides action based OpenXR calls (aka modern VR and AR).
 * <p>
 * See <a href="https://registry.khronos.org/OpenXR/specs/1.0/html/xrspec.html#_action_overview">khronos spec action_overview</a>
 */
public class ActionOpenXRState extends BaseAppState{

    public static final String ID = "ActionBasedOpenXRState";

    private static final Quaternion HALF_ROTATION_ABOUT_Y = new Quaternion();

    private static final Logger logger = Logger.getLogger(ActionOpenXRState.class.getName());

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
     * A map of input names (e.g. /user/hand/right) to the handle used to address it.
     * <p>
     * Note that null is a special case that maps to VR.k_ulInvalidInputValueHandle and means "any input"
     */
    private final Map<String, Long> inputHandles = new HashMap<>();

    private List<String> bothHandActionSets = new ArrayList<>(0);

    private List<String> leftHandActionSets = new ArrayList<>(0);

    private List<String> rightHandActionSets = new ArrayList<>(0);

    private final XrSession xrSession;
    private final XrInstance xrInstance;

    private XrActionSet activeActionSet;

    XrAction teleportAction;

    static {
        HALF_ROTATION_ABOUT_Y.fromAngleAxis(FastMath.PI, Vector3f.UNIT_Y);
    }

    {
        //inputHandles.put(null, VR.k_ulInvalidInputValueHandle);
    }

    public ActionOpenXRState(XrSession xrSession, XrInstance xrInstance){
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
     * Note that registering an actions manifest will deactivate legacy inputs (i.e. methods such as isButtonDown
     * will no longer work
     * <p>
     * See <a href="https://github.com/ValveSoftware/openvr/wiki/Action-manifest">https://github.com/ValveSoftware/openvr/wiki/Action-manifest</a>
     * for documentation on how to create an action manifest
     * <p>
     * This option is only relevant to OpenVR
     *
     * @param actionManifestAbsolutePath
     *          the absolute file path to an actions manifest
     * @param startingActiveActionSets
     *          the actions in the manifest are divided into action sets (groups) by their prefix (e.g. "/actions/main").
     *          These action sets can be turned off and on per frame. This argument sets the action set that will be
     *          active now. The active action sets can be later be changed by calling {@link #setActiveActionSetsBothHands}.
     *          Note that at present only a single set at a time is supported
     *
     */
    public void registerActions(){
        //see https://registry.khronos.org/OpenXR/specs/1.0/html/xrspec.html#_action_overview

        XrActionSetCreateInfo actionSetCreate = XrActionSetCreateInfo.create();
        actionSetCreate.actionSetName(stringToByte("test"));
        actionSetCreate.localizedActionSetName(stringToByte("testTranslation"));
        actionSetCreate.priority(0);

        PointerBuffer actionSetPointer = BufferUtils.createPointerBuffer(1);
        withResponseCodeLogging("Create action set", XR10.xrCreateActionSet(xrInstance, actionSetCreate, actionSetPointer));
        actionSetCreate.close();

        XrActionSet actionSet = new XrActionSet(actionSetPointer.get(), xrInstance);
        activeActionSet = actionSet;

        XrActionCreateInfo xrActionCreateInfo = XrActionCreateInfo.create();
        xrActionCreateInfo.actionName(stringToByte("teleport"));
        xrActionCreateInfo.actionType(ActionType.BOOLEAN.getOpenXrOption());
        xrActionCreateInfo.localizedActionName(stringToByte("teleportTranslated"));
        PointerBuffer actionPointer = BufferUtils.createPointerBuffer(1);
        withResponseCodeLogging("xrStringToPath", XR10.xrCreateAction(actionSet, xrActionCreateInfo, actionPointer));
        XrAction action = new XrAction(actionPointer.get(), actionSet);
        teleportAction = action;

        xrActionCreateInfo.close();

        LongBuffer oculusProfilePath = BufferUtils.createLongBuffer(1);
        withResponseCodeLogging("xrStringToPath", XR10.xrStringToPath(xrInstance, OculusTouchController.PROFILE, oculusProfilePath));

        //XrcAtionSuggestedBinding suggestedBinding = XrActionSuggestedBinding.create();
        LongBuffer xClickPathBuffer = BufferUtils.createLongBuffer(1);
        withResponseCodeLogging("xrStringToPath",XR10.xrStringToPath(xrInstance,OculusTouchController.pathBuilder().leftHand().xClick(), xClickPathBuffer));
        //suggestedBinding.set(action, XR10.xrStringToPath(xrInstance,OculusTouchController.pathBuilder().leftHand().xTouch(), xClickPathBuffer));

        XrActionSuggestedBinding.Buffer suggestedBindingsBuffer = XrActionSuggestedBinding.calloc(1);
        suggestedBindingsBuffer.action(action);
        suggestedBindingsBuffer.binding(xClickPathBuffer.get());


        XrInteractionProfileSuggestedBinding xrInteractionProfileSuggestedBinding = XrInteractionProfileSuggestedBinding.calloc()
                .type(XR10.XR_TYPE_INTERACTION_PROFILE_SUGGESTED_BINDING)
                .next(NULL)
                .interactionProfile(oculusProfilePath.get())
                .suggestedBindings(suggestedBindingsBuffer);

        //xrInteractionProfileSuggestedBinding.suggestedBindings(suggestedBindingsBuffer);
        //xrInteractionProfileSuggestedBinding.interactionProfile(oculusProfilePath.get());

        //the below is the line returning the error code
        withResponseCodeLogging("xrSuggestInteractionProfileBindings", XR10.xrSuggestInteractionProfileBindings(xrInstance, xrInteractionProfileSuggestedBinding));

        actionSetPointer.rewind();
        XrSessionActionSetsAttachInfo actionSetsAttachInfo = XrSessionActionSetsAttachInfo.create();
        actionSetsAttachInfo.actionSets(actionSetPointer);

        withResponseCodeLogging("xrAttachSessionActionSets", XR10.xrAttachSessionActionSets(xrSession, actionSetsAttachInfo));
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
        if (errorCode != 0){
            ByteBuffer buffer = BufferUtils.createByteBuffer(XR10.XR_MAX_RESULT_STRING_SIZE);
            XR10.xrResultToString(xrInstance, errorCode, buffer);

            String message = MemoryUtil.memUTF8(buffer, MemoryUtil.memLengthNT1(buffer));
            logger.warning("XRNATIVE"+message);

            CallResponseCode fullErrorDetails = CallResponseCode.getResponseCode(errorCode);
            if (fullErrorDetails.isAnErrorCondition()){
                logger.warning(fullErrorDetails.getFormattedErrorMessage() + " Occurred during  " + eventText);
            }else{
                if (logger.isLoggable(Level.INFO)){
                    logger.info(fullErrorDetails.getFormattedErrorMessage() + " Occurred during " + eventText);
                }
            }
        }
    }

    /**
     * This sets action sets active for all hands
     * @param actionSets the action sets to set as active
     */
    public void setActiveActionSetsBothHands(String... actionSets){

    }

    /**
     * This sets action sets active for the left hand only
     * <p>
     * Note that setting an action to left and right (or left and both) is equivalent to setting it to both
     * @param actionSets the action sets to set as active
     */
    public void setActiveActionSetsLeftHand(String... actionSets){

    }

    /**
     * This sets action sets active for the right hand only
     * <p>
     * Note that setting an action to left and right (or right and both) is equivalent to setting it to both
     *
     * @param actionSets the action sets to set as active
     */
    public void setActiveActionSetsRightHand(String... actionSets){

    }

    /**
     * Gets the current state of the action (abstract version of a button press).
     * <p>
     * This is called for digital style actions (a button is pressed, or not)
     * <p>
     * {@link #registerActions} must have been called before using this method.
     *
     * @param actionName The name of the action. E.g. /actions/main/in/openInventory
     * @return the DigitalActionState that has details on if the state has changed, what the state is etc.

    public DigitalActionState getDigitalActionState(String actionName){
        return getDigitalActionState(actionName, null);
    }
     */

    /**
     * Gets the current state of the action (abstract version of a button press).
     * <p>
     * This is called for digital style actions (a button is pressed, or not)
     * <p>
     * This method is commonly called when it is important which hand the action is found on. For example while
     * holding a weapon a button may be bound to "eject magazine" to allow you to load a new one, but that would only
     * want to take effect on the hand that is holding the weapon
     * <p>
     * Note that restrictToInput only restricts, it must still be bound to the input you want to receive the input from in
     * the action manifest default bindings.
     * <p>
     * {@link #registerActions} must have been called before using this method.
     *
     * @param actionName The name of the action. E.g. /actions/main/in/openInventory
     * @param restrictToInput the input to restrict the action to. E.g. /user/hand/right. Or null, which means "any input"
     * @return the DigitalActionState that has details on if the state has changed, what the state is etc.

    public DigitalActionState getDigitalActionState(String actionName, String restrictToInput){
    }
     */
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
     * This is called for analog style actions (most commonly joysticks, but button pressure can also be mapped in analog).
     * <p>
     * This method is commonly called when it's not important which hand the action is bound to (e.g. if the thumb stick
     * is controlling a third-person character in-game that could be bound to either left or right hand and that would
     * not matter).
     * <p>
     * If the handedness matters use {@link #getAnalogActionState(String, String)}
     *
     * {@link #registerActions} must have been called before using this method.
     *
     * @param actionName The name of the action. E.g. /actions/main/in/openInventory
     * @return the AnalogActionState that has details on how much the state has changed, what the state is etc.
     */
    /*
    public AnalogActionState getAnalogActionState( String actionName ){
        return getAnalogActionState(actionName, null);
    }
    */
    /**
     * Gets the current state of the action (abstract version of a button press).
     * <p>
     * This is called for analog style actions (most commonly joysticks, but button pressure can also be mapped in analog).
     * <p>
     * This method is commonly called when it is important which hand the action is found on. For example an "in universe"
     * joystick that has a hat control might (while you are holding it) bind to the on-controller hat, but only on the hand
     * holding it
     * <p>
     * Note that restrictToInput only restricts, it must still be bound to the input you want to receive the input from in
     * the action manifest default bindings.
     * <p>
     * {@link #registerActions} must have been called before using this method.
     *
     * @param actionName The name of the action. E.g. /actions/main/in/openInventory
     * @param restrictToInput the input to restrict the action to. E.g. /user/hand/right. Or null, which means "any input"
     * @return the AnalogActionState that has details on how much the state has changed, what the state is etc.
     */
    /*
    public AnalogActionState getAnalogActionState(String actionName, String restrictToInput ){
        assert inputMode == InputMode.ACTION_BASED : "registerActionManifest must be called before attempting to fetch action states";

        LWJGLOpenVRAnalogActionData actionDataObjects = analogActions.get(actionName);
        if (actionDataObjects == null){
            //this is the first time the action has been used. We must obtain a handle to it to efficiently fetch it in future
            long handle = fetchActionHandle(actionName);
            actionDataObjects = new LWJGLOpenVRAnalogActionData(actionName, handle, InputAnalogActionData.create());
            analogActions.put(actionName, actionDataObjects);
        }
        int errorCode = VRInput.VRInput_GetAnalogActionData(actionDataObjects.actionHandle, actionDataObjects.actionData, getOrFetchInputHandle(restrictToInput));

        if (errorCode == VR.EVRInputError_VRInputError_WrongType){
            throw new WrongActionTypeException("Attempted to fetch a non-analog state as if it is analog");
        }else if (errorCode!=0){
            logger.warning( "An error code of " + errorCode + " was reported while fetching an action state for " + actionName );
        }

        return new AnalogActionState(actionDataObjects.actionData.x(), actionDataObjects.actionData.y(), actionDataObjects.actionData.z(), actionDataObjects.actionData.deltaX(), actionDataObjects.actionData.deltaY(), actionDataObjects.actionData.deltaZ());
    }
    */

    /**
     * Triggers a haptic action (aka a vibration).
     * <p>
     * Note if you want a haptic action in only one hand that is done either by only binding the action to one hand in
     * the action manifest's standard bindings or by binding to both and using {@link #triggerHapticAction(String, float, float, float, String)}
     * to control which input it gets set to at run time
     *
     * @param actionName The name of the action. Will be something like /actions/main/out/vibrate
     * @param duration how long in seconds the
     * @param frequency in cycles per second
     * @param amplitude between 0 and 1
     */
    public void triggerHapticAction( String actionName, float duration, float frequency, float amplitude){
        triggerHapticAction( actionName, duration, frequency, amplitude, null );
    }

    /**
     * Triggers a haptic action (aka a vibration) restricted to just one input (e.g. left or right hand).
     * <p>
     * Note that restrictToInput only restricts, it must still be bound to the input you want to send the haptic to in
     * the action manifest default bindings.
     * <p>
     * This method is typically used to bind the haptic to both hands then decide at run time which hand to sent to     *
     *
     * @param actionName The name of the action. Will be something like /actions/main/out/vibrate
     * @param duration how long in seconds the
     * @param frequency in cycles per second
     * @param amplitude between 0 and 1
     * @param restrictToInput the input to restrict the action to. E.g. /user/hand/right, /user/hand/left. Or null, which means "any input"
     */
    public void triggerHapticAction(String actionName, float duration, float frequency, float amplitude, String restrictToInput ){

    }

    @Override
    public void update(float tpf){
        super.update(tpf);

        if (activeActionSet==null){
            return;
        }

        XrActionsSyncInfo syncInfo = XrActionsSyncInfo.create();
        syncInfo.countActiveActionSets(1);
        ByteBuffer activeActionSetBuffer = BufferUtils.createByteBuffer(XrActiveActionSet.SIZEOF*1);
        XrActiveActionSet.Buffer activeActionSets = new XrActiveActionSet.Buffer(activeActionSetBuffer);
        activeActionSets.actionSet(activeActionSet);

        syncInfo.activeActionSets(activeActionSets);

        withResponseCodeLogging("xrSyncActions", XR10.xrSyncActions(xrSession, syncInfo));

        XrActionStateBoolean teleportState = XrActionStateBoolean.create();
        XrActionStateGetInfo teleportInfo = XrActionStateGetInfo.create();
        teleportInfo.action(teleportAction);

        withResponseCodeLogging("getActionState", XR10.xrGetActionStateBoolean(xrSession, teleportInfo, teleportState));
        if (teleportState.changedSinceLastSync()){
            System.out.println("Value " + teleportState.currentState());
        }
    }

    /**
     * Converts an action name (as it appears in the action manifest) to a handle (long) that the rest of the
     * lwjgl (and openVR) wants to talk in
     * @param actionName The name of the action. Will be something like /actions/main/in/openInventory
     * @return a long that is the handle that can be used to refer to the action
     */
    private long fetchActionHandle( String actionName ){
        //do we need to do this?
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Given an input name returns the handle to address it.
     * <p>
     * If a cached handle is available it is returned, if not it is fetched from openVr
     *
     * @param inputName the input name, e.g. /user/hand/right. Or null, which means "any input"
     * @return the handle
     */
    public long getOrFetchInputHandle( String inputName ){
        //do we need to do this?
        throw new UnsupportedOperationException("Not supported yet.");
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

    /*
    private VRActiveActionSet.Buffer getOrBuildActionSets(){

        if (activeActionSets != null){
            return activeActionSets;
        }

        Map<String, String> actionSetAndRestriction = new HashMap<>();

        Set<String> allActionSets = new HashSet<>();
        allActionSets.addAll(leftHandActionSets);
        allActionSets.addAll(rightHandActionSets);
        allActionSets.addAll(bothHandActionSets);

        for(String actionSet: allActionSets){
            if (bothHandActionSets.contains(actionSet) || (leftHandActionSets.contains(actionSet) && rightHandActionSets.contains(actionSet))){
                actionSetAndRestriction.put(actionSet, null);
            }else if (leftHandActionSets.contains(actionSet)){
                actionSetAndRestriction.put(actionSet, HandSide.LEFT.restrictToInputString);
            }else{
                actionSetAndRestriction.put(actionSet, HandSide.RIGHT.restrictToInputString);
            }
        }

        actionSetAndRestriction.keySet().forEach(actionSet -> {
            long actionSetHandle;
            if(!actionSetHandles.containsKey(actionSet)){
                LongBuffer longBuffer = BufferUtils.createLongBuffer(1);
                int errorCode = VRInput.VRInput_GetActionHandle(actionSet, longBuffer);
                if(errorCode != 0){
                    logger.warning("An error code of " + errorCode + " was reported while fetching an action set handle for " + actionSet);
                }
                actionSetHandle = longBuffer.get(0);
                actionSetHandles.put(actionSet, actionSetHandle);
            }
        });

        activeActionSets = VRActiveActionSet.create(actionSetAndRestriction.size());

        Iterator<Map.Entry<String, String>> iterator = actionSetAndRestriction.entrySet().iterator();

        for(VRActiveActionSet actionSetItem : activeActionSets){
            Map.Entry<String, String> entrySetAndRestriction = iterator.next();
            actionSetItem.ulActionSet(actionSetHandles.get(entrySetAndRestriction.getKey()));
            actionSetItem.ulRestrictedToDevice(getOrFetchInputHandle(entrySetAndRestriction.getValue()));
        }

        return activeActionSets;
    }
    */

}

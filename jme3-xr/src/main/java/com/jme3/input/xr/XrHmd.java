package com.jme3.input.xr;

import java.util.ArrayList;

import com.jme3.actions.OpenXRActionState;
import com.jme3.app.SimpleApplication;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;
import com.jme3.system.lwjgl.LwjglWindowXr;
import com.jme3.system.lwjgl.openxr.HelloOpenXRGL;

public class XrHmd
{
	SimpleApplication app;
	Eye leftEye;
	Eye rightEye;
	ArrayList<XrListener.OrientationListener> hmdListeners = new ArrayList<>();
	ArrayList<XrListener.ButtonPressedListener> contr1Listeners = new ArrayList<>();
	ArrayList<XrListener.ButtonPressedListener> contr2Listeners = new ArrayList<>();
	
	public XrHmd(SimpleApplication app)
	{
		this.app = app;
		leftEye = new Eye(app);
		rightEye = new Eye(app);
	}

	public Eye getLeftEye() { return leftEye; }
	public Eye getRightEye() { return rightEye; }
	
	public ArrayList<XrListener.OrientationListener> getHmdOrientationListeners() { return hmdListeners; }
	public ArrayList<XrListener.ButtonPressedListener> getContr1ButtonPressedListeners() { return contr1Listeners; }
	public ArrayList<XrListener.ButtonPressedListener> getContr2ButtonPressedListeners() { return contr2Listeners; }
	
	public void onUpdateHmdOrientation(Vector3f viewPos, Quaternion viewRot)
	{
		for (XrListener.OrientationListener l : hmdListeners) { l.onUpdateOrientation(viewPos, viewRot); }
	}
	
	/** Must be called in main function before init.
	 * @param s The appSettings that must be used with app.setSettings(s). */
	public static void setRendererForSettings(AppSettings s)
	{
		s.setRenderer("CUSTOM" + com.jme3.system.lwjgl.LwjglWindowXr.class.getName()); //see JmeDesktopSystem.newContext(...)
	}
	
	/** Must be called in simpleInitApp-function of SimpleApplication.
	 * @return The head-mounted-device object. */
	public static XrHmd initHmd(SimpleApplication app)
	{
		XrHmd xrHmd = new XrHmd(app);
		HelloOpenXRGL xr = ((LwjglWindowXr)app.getContext()).getXr();
		xr.setHmd(xrHmd);

		OpenXRActionState openXRActionState = new OpenXRActionState(xr.getXrSession(), xr.getXrInstance(), xr);
		app.getStateManager().attach(openXRActionState);

		return xrHmd;
	}
}

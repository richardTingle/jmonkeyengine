package com.jme3.input.xr;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.FrameBuffer.FrameBufferTarget;
import com.jme3.texture.Image.Format;

public class Eye {
	static int index = 0;
	private final SimpleApplication app;
    private Texture2D offTex;
    private final Geometry offGeo;

    private Camera offCamera;

    float fovX = -1;
    float fovY = -1;
    
    public Eye(SimpleApplication app)
    {
    	this.app = app;
    	setupOffscreenView(app);
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", offTex);
        
        offGeo = new Geometry("box", new Box(1, 1, 1));
        offGeo.setMaterial(mat);
    }

    public void setPosition(Vector3f newPosition){
        offCamera.setLocation(newPosition);
    }

    public void setRotation(Quaternion newRotation){
        offCamera.setRotation(newRotation);
    }

    /**
     * Sets the field of view for the eye. Angles in radians.
     */
    public void setFieldOfView(float fovX, float fovY){
        if (this.fovX!= fovX || this.fovY!= fovY){
            this.fovX = fovX;
            this.fovY = fovY;
            offCamera.setFrustumPerspective(FastMath.RAD_TO_DEG*fovY, fovX/fovY, 0.1f, 1000f);
        }
    }

    private void setupOffscreenView(SimpleApplication app)
    {
        //should we be asking openXR for the resolution?
    	int w = app.getContext().getSettings().getWidth();
    	int h = app.getContext().getSettings().getHeight();
        offCamera = new Camera(w, h);

        ViewPort offView = app.getRenderManager().createPreView("OffscreenViewX" + (index++), offCamera);
        offView.setClearFlags(true, true, true);
        offView.setBackgroundColor(ColorRGBA.DarkGray);
        FrameBuffer offBuffer = new FrameBuffer(w, h, 1);

        //setup framebuffer's texture
        offTex = new Texture2D(w, h, Format.RGBA8);
        offTex.setMinFilter(Texture.MinFilter.Trilinear);
        offTex.setMagFilter(Texture.MagFilter.Bilinear);

        //setup framebuffer to use texture
        offBuffer.setDepthTarget(FrameBufferTarget.newTarget(Format.Depth));
        offBuffer.addColorTarget(FrameBufferTarget.newTarget(offTex));

        //set viewport to render to offscreen framebuffer
        offView.setOutputFrameBuffer(offBuffer);
        offView.attachScene(app.getRootNode());
    }
    
    public void render()
    {
    	app.getRenderManager().renderGeometry(offGeo);
    }
}

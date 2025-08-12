package org.jmonkeyengine.screenshottests.light;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import org.jmonkeyengine.screenshottests.testframework.ScreenshotTestBase;
import org.junit.jupiter.api.Test;


public class GlobalLights extends ScreenshotTestBase {
    
    /**
     * This test tests global lights; those are lights which despite being attached to a node light the entire scene
     * <p>
     * (unlike normal lights which only light their children). This test tests adding and removing global lights to enure that 
     * bookkeeping is done correctly.
     * </p>
     */
    @Test
    public void testGlobalLights() {
        screenshotTest(new BaseAppState() {

            final PointLight globalPointLight = new PointLight(true);
            final Node lightsAttachedNode = new Node("lightsAttachedNode");

            @Override
            protected void initialize(Application app) {
                SimpleApplication application = (SimpleApplication)app;

                // an outrageous distance, so if the node's positions aren't taken into account,
                // everything will be obvious
                Vector3f testOffset = new Vector3f(0,0,0);

                lightsAttachedNode.setLocalTranslation(testOffset);

                Node lightsNotAttachedNode = new Node("lightsNotAttachedNode");
                lightsNotAttachedNode.setLocalTranslation(testOffset);

                Geometry litByAll = createLitWhiteCube(app.getAssetManager(), "litByAll");
                litByAll.setLocalTranslation(2,0,-1);
                lightsAttachedNode.attachChild(litByAll);

                Geometry litOnlyByGlobal = createLitWhiteCube(app.getAssetManager(), "litOnlyByGlobal");
                litOnlyByGlobal.setLocalTranslation(-2,0,-1);
                lightsNotAttachedNode.attachChild(litOnlyByGlobal);

                PointLight localPointLight = new PointLight();
                localPointLight.setColor(ColorRGBA.Red);

                globalPointLight.setColor(ColorRGBA.Green);


                application.getCamera().setLocation(testOffset.add(new Vector3f(0,0,10)));
                application.getCamera().lookAt(testOffset, Vector3f.UNIT_Y);

                application.getRootNode().attachChild(lightsAttachedNode);
                application.getRootNode().attachChild(lightsNotAttachedNode);

                lightsAttachedNode.addLight(localPointLight);
                lightsAttachedNode.addLight(globalPointLight);

                application.getRootNode().addLight(new AmbientLight(new ColorRGBA(0.01f,0.01f, 0.01f, 1)));
            }

            public void attachGlobalLight(){
                lightsAttachedNode.addLight(globalPointLight);
            }

            public void detachGlobalLight(){
                lightsAttachedNode.removeLight(globalPointLight);
            }

            @Override
            protected void cleanup(Application app) {}

            @Override
            protected void onEnable() {}

            @Override
            protected void onDisable() {}
        }).setFramesToTakeScreenshotsOn(200)
          .run();
    }

    private static Geometry createLitWhiteCube(AssetManager assetManager, String name) {
        Box box = new Box(1, 1, 1);
        Geometry cube = new Geometry(name, box);
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setColor("Diffuse", ColorRGBA.White);
        mat.setColor("Ambient", ColorRGBA.White);
        mat.setBoolean("UseMaterialColors", true);
        cube.setMaterial(mat);
        return cube;
    }


}

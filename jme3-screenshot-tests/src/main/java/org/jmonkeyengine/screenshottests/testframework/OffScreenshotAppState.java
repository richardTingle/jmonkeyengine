package org.jmonkeyengine.screenshottests.testframework;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.post.SceneProcessor;
import com.jme3.profile.AppProfiler;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.system.JmeSystem;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class OffScreenshotAppState extends AbstractAppState implements SceneProcessor {

    Texture2D renderTexture;

    Renderer renderer;

    FrameBuffer frameBuffer;

    private boolean capture = false;

    ByteBuffer outBuf;

    public void takeScreenshot() {
        capture = true;
    }

    public OffScreenshotAppState(Texture2D renderTexture, FrameBuffer frameBuffer) {
        this.renderTexture = renderTexture;
        this.frameBuffer = frameBuffer;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        renderer = app.getRenderManager().getRenderer();
        outBuf = BufferUtils.createByteBuffer(renderTexture.getImage().getWidth() * renderTexture.getImage().getHeight() * 4);
    }

    @Override
    public void initialize(RenderManager rm, ViewPort vp) {

    }

    @Override
    public void reshape(ViewPort vp, int w, int h) {

    }

    @Override
    public void preFrame(float tpf) {

    }

    @Override
    public void postQueue(RenderQueue rq) {

    }

    @Override
    public void postRender() {
        super.postRender();
        if (capture) {
            capture = false;

            renderer.readFrameBuffer(frameBuffer, outBuf);
            try {
                FileOutputStream fileOutBuf = new FileOutputStream("C:\\Users\\richa\\Documents\\test.png");
                JmeSystem.writeImageFile(fileOutBuf, "png",outBuf, renderTexture.getImage().getWidth(), renderTexture.getImage().getHeight());
            }catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void postFrame(FrameBuffer out) {
int a=0;
    }

    @Override
    public void setProfiler(AppProfiler profiler) {

    }

}

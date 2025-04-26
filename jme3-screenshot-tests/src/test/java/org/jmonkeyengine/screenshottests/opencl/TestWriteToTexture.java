/*
 * Copyright (c) 2024 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.jmonkeyengine.screenshottests.opencl;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.Vector2f;
import com.jme3.opencl.*;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;
import org.jmonkeyengine.screenshottests.testframework.ScreenshotTestBase;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

/**
 * Screenshot test for the OpenCL texture writing functionality.
 * 
 * <p>This test uses OpenCL to generate a Julia set fractal and render it to a texture.
 * The C parameter of the Julia set is changed during the test to demonstrate how
 * the fractal changes with different parameters.
 *
 * @author Richard Tingle (screenshot test adaptation)
 */
public class TestWriteToTexture extends ScreenshotTestBase {

    private static final Logger LOG = Logger.getLogger(TestWriteToTexture.class.getName());

    /**
     * This test creates a scene with a Julia set fractal rendered using OpenCL.
     * It takes a screenshot at frame 5 with the initial C parameter,
     * changes the C.x value at frame 10, and takes another screenshot at frame 15.
     */
    @Test
    public void testWriteToTexture() {
        screenshotTest(new BaseAppState() {
            private Texture2D tex;
            private int initCounter;
            private Context clContext;
            private CommandQueue clQueue;
            private Kernel kernel;
            private Vector2f C;
            private Image texCL;
            private int frameCount = 0;

            @Override
            protected void initialize(Application app) {
                SimpleApplication simpleApplication = (SimpleApplication) app;
                
                // Initialize OpenCL
                initOpenCL1(app);
                
                // Create texture and picture
                AppSettings settings = app.getContext().getSettings();
                tex = new Texture2D(settings.getWidth(), settings.getHeight(), 1, com.jme3.texture.Image.Format.RGBA8);
                Picture pic = new Picture("julia");
                pic.setTexture(app.getAssetManager(), tex, true);
                pic.setPosition(0, 0);
                pic.setWidth(settings.getWidth());
                pic.setHeight(settings.getHeight());
                simpleApplication.getGuiNode().attachChild(pic);
                
                initCounter = 0;
            }

            private void initOpenCL1(Application app) {
                clContext = app.getContext().getOpenCLContext();
                clQueue = clContext.createQueue().register();
                ProgramCache programCache = new ProgramCache(clContext);
                
                // Create kernel
                String cacheID = getClass().getName() + ".Julia";
                Program program = programCache.loadFromCache(cacheID);
                if (program == null) {
                    LOG.info("Program not loaded from cache, create from sources instead");
                    program = clContext.createProgramFromSourceFiles(app.getAssetManager(), "jme3test/opencl/JuliaSet.cl");
                    program.build();
                    programCache.saveToCache(cacheID, program);
                }
                program.register();
                kernel = program.createKernel("JuliaSet").register();
                C = new Vector2f(0.12f, -0.2f);
            }

            private void initOpenCL2() {
                // Bind image to OpenCL
                texCL = clContext.bindImage(tex, MemoryAccess.WRITE_ONLY).register();
            }

            private void updateOpenCL() {
                AppSettings settings = getApplication().getContext().getSettings();
                
                // Acquire resource
                texCL.acquireImageForSharingNoEvent(clQueue);
                
                // Execute kernel
                Kernel.WorkSize ws = new Kernel.WorkSize(settings.getWidth(), settings.getHeight());
                kernel.Run1NoEvent(clQueue, ws, texCL, C, 16);
                
                // Release resource
                texCL.releaseImageForSharingNoEvent(clQueue);
            }

            @Override
            protected void cleanup(Application app) {
            }

            @Override
            protected void onEnable() {
            }

            @Override
            protected void onDisable() {
            }

            @Override
            public void update(float tpf) {
                super.update(tpf);
                
                frameCount++;
                
                if (initCounter < 2) {
                    initCounter++;
                } else if (initCounter == 2) {
                    // When initCounter reaches 2, the scene was drawn once and the texture was uploaded to the GPU
                    // Then we can bind the texture to OpenCL
                    initOpenCL2();
                    updateOpenCL();
                    initCounter = 3;
                } else {
                    updateOpenCL();
                }
                
                // Change C.x value at frame 10
                if (frameCount == 10) {
                    C.x = 0.25f;  // Change to a different value to see a different fractal (for the second screenshot test)
                }
            }
        })
        .withOpenCLSupport()
        .setFramesToTakeScreenshotsOn(5, 15)
        .run();
    }
}
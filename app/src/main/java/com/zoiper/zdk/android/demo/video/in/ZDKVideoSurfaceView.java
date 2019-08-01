package com.zoiper.zdk.android.demo.video.in;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * ZDKVideoSurfaceView
 *
 * @since 19/02/2019
 */
public class ZDKVideoSurfaceView extends GLSurfaceView implements LifecycleObserver {

    private static final int EGL_CONTEXT_CLIENT_VERSION = 2;

    private LibraryVideoRenderer renderer;

    public ZDKVideoSurfaceView(Context context) {
        super(context);
        init();
    }

    public ZDKVideoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        renderer = new LibraryVideoRenderer();

        setEGLContextClientVersion(EGL_CONTEXT_CLIENT_VERSION);
        setPreserveEGLContextOnPause(true);

        setRenderer(renderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private void onActivityPause(){
        this.onPause();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private void onActivityResume(){
        this.onResume();
    }

    /**
     * This method is here just to make using a method reference easier
     *
     * @param bytes The actual info in YUV I420 format
     * @param ignoredLength This would be the length of the array, we don't really need it.
     * @param width of the image
     * @param height of the image
     */
    @SuppressWarnings("unused")
    public void renderI420YUV(byte[] bytes, int ignoredLength, int width, int height){
        renderI420YUV(bytes, width, height);
    }

    /**
     * Render a frame
     *
     * @param bytes The actual info in YUV I420 format
     * @param width of the image
     * @param height of the image
     */
    public void renderI420YUV(byte[] bytes, int width, int height){
        renderer.renderI420YUV(bytes, width, height);
        requestRender();
    }
}

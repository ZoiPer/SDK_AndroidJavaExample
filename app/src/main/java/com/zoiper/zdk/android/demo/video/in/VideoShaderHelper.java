package com.zoiper.zdk.android.demo.video.in;

import android.util.Log;

/**
 * Helper class to handle vertex shader parameter calculations, so that a
 * texture can be drawn in a target area preserving the aspect ratio.
 */
class VideoShaderHelper {

    private static final String TAG = "VideoShaderHelper";

    /*private static final boolean DBG = (Config.DBG_LEVEL >= 1);
    private static final boolean VDBG = (Config.DBG_LEVEL >= 2);*/

    private static final boolean DBG = true;
    private static final boolean VDBG = true;

    private float fragmentStartX, fragmentEndX;
    private float fragmentStartY, fragmentEndY;
    private float vertexStartX, vertexEndX;
    private float vertexStartY, vertexEndY;

    enum FitMode {FIT_PAD, FIT_STRETCH, FIT_CROP}

    VideoShaderHelper(float sourceWidth,
                      float sourceHeight,
                      float targetWidth,
                      float targetHeight,
                      float targetCenterX,
                      float targetCenterY,
                      float maxSizePercentage,
                      FitMode mode) {
        if (DBG) {
            Log.d(TAG, "calculateDisplayPosition");
        }

        if (VDBG) {
            Log.d(TAG, "sourceWidth: " + sourceWidth + ", sourceHeight: " + sourceHeight);
            Log.d(TAG, "targetWidth: " + targetWidth + ", targetHeight: " + targetHeight);
            Log.d(TAG,
                    "targetPositionX: " + targetCenterX + ", targetPositionY: " + targetCenterY);
            Log.d(TAG, "maxSizePercentage: " + maxSizePercentage);
            Log.d(TAG, "mode: " + mode.toString());
        }

        maxSizePercentage = maxSizePercentage / 100.0f;

        float vertexWidth = 0, vertexHeight = 0;
        float fragmentWidth = 0, fragmentHeight = 0;

        switch (mode) {
            case FIT_PAD:
                vertexHeight = Math.min(1.0f,
                        ((sourceHeight / targetHeight) * targetWidth) /
                                sourceWidth) * maxSizePercentage;
                vertexWidth = Math.min(1.0f,
                        ((sourceWidth / targetWidth) * targetHeight) /
                                sourceHeight) * maxSizePercentage;
                fragmentWidth = 1;
                fragmentHeight = 1;
                break;

            case FIT_CROP:
                vertexHeight = 1 * maxSizePercentage;
                vertexWidth = 1 * maxSizePercentage;
                fragmentWidth = Math.min(1.0f,
                        targetHeight /
                                (sourceHeight * (targetWidth / sourceWidth)));
                fragmentHeight = Math.min(1.0f,
                        targetWidth /
                                (sourceWidth * (targetHeight / sourceHeight)));
                break;

            case FIT_STRETCH:
                break;
        }

        /* vertex coordinates are in range [-1;1] */
        vertexStartX = targetCenterX - vertexWidth;
        vertexEndX = targetCenterX + vertexWidth;
        vertexStartY = targetCenterY - vertexHeight;
        vertexEndY = targetCenterY + vertexHeight;

        /* fragment coordinates are in range [0;1] */
        fragmentStartX = 0.5f - (fragmentWidth * 0.5f);
        fragmentEndX = 0.5f + (fragmentWidth * 0.5f);
        fragmentStartY = 0.5f - (fragmentHeight * 0.5f);
        fragmentEndY = 0.5f + (fragmentHeight * 0.5f);

        if (DBG) {
            Log.d(TAG, "vertexStartX: " + vertexStartX + ", vertexStartY: " + vertexStartY);
            Log.d(TAG, "vertexStartX: " + vertexEndX + ", vertexEndY: " + vertexEndY);
            Log.d(TAG,
                    "fragmentStartX: " + fragmentStartX + ", fragmentStartY: " + fragmentStartY);
            Log.d(TAG, "fragmentEndX: " + fragmentEndX + ", oglEndY: " + fragmentEndY);
        }
    }

    /**
     * Gets a shader that is not rotated or mirrored
     */
    float[] getVertexShaderNormal() {
        return new float[] {vertexStartX,
                vertexEndY,
                vertexEndX,
                vertexEndY,
                vertexStartX,
                vertexStartY,
                vertexEndX,
                vertexStartY};
    }

    float[] getFragmentShaderNormal() {
        return new float[] {fragmentStartX,
                fragmentStartY,
                fragmentEndX,
                fragmentStartY,
                fragmentStartX,
                fragmentEndY,
                fragmentEndX,
                fragmentEndY};
    }
}

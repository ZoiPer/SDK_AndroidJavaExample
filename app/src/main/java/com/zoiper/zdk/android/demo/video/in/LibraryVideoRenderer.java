package com.zoiper.zdk.android.demo.video.in;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * LibraryVideoRenderer
 *
 * @since 18/02/2019
 */
public class LibraryVideoRenderer implements GLSurfaceView.Renderer {

    private static final boolean DEBUG_LOGGING = false;

    private static final String TAG = "LibVideoRenderer";

    private static final String strFragmentShader = "precision mediump float;\n" +
            "varying vec2 texCoord;\n" +
            "\n" +
            "uniform sampler2D SamplerY;\n" +
            "uniform sampler2D SamplerU;\n" +
            "uniform sampler2D SamplerV;\n" +
            "\n" +
            "const mat3 yuv2rgb = mat3(1, 0, 1.2802,1, -0.214821, -0.380589,1, 2.127982, 0);\n" +
            "\n" +
            "void main() {    \n" +
            "    vec3 yuv = vec3(1.1643 * (texture2D(SamplerY, texCoord).r - 0.0625),\n" +
            "                    texture2D(SamplerU, texCoord).r - 0.5,\n" +
            "                    texture2D(SamplerV, texCoord).r - 0.5);\n" +
            "    vec3 rgb = yuv * yuv2rgb;\n" +
            "    gl_FragColor = vec4(rgb, 1.0);\n" +
            "}";

    private static final String strVertexShader = "attribute vec2 vPosition;\n" +
            "attribute vec2 vTexCoord;\n" +
            "varying vec2 texCoord;\n" +
            "void main() {\n" +
            "  texCoord = vTexCoord;\n" +
            "  gl_Position = vec4 ( vPosition.x, vPosition.y, -0.75, 1.0 );\n" +
            "}";

    private int mProgram;

    private int[] mTextureU = new int[1];
    private int[] mTextureV = new int[1];
    private int[] mTextureY = new int[1];

    private byte[] yBuffer;
    private byte[] uBuffer;
    private byte[] vBuffer;

    private int actualSourceWidth = 0;
    private int actualSourceHeight = 0;

    private boolean started;

    private int surfaceWidth;
    private int surfaceHeight;

    private FloatBuffer oglDisplayFragmentShader;
    private FloatBuffer oglDisplayVertexShader;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glEnable(GLES20.GL_BLEND);
        if(DEBUG_LOGGING) Log.d(TAG, "onSurfaceCreated - glEnable error: " + GLES20.glGetError());
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        if(DEBUG_LOGGING) Log.d(TAG, "onSurfaceCreated - glDisable error: " + GLES20.glGetError());
        GLES20.glDisable(GLES20.GL_STENCIL_TEST);
        if(DEBUG_LOGGING) Log.d(TAG, "onSurfaceCreated - glDisable error: " + GLES20.glGetError());
        GLES20.glDisable(GLES20.GL_DITHER);
        if(DEBUG_LOGGING) Log.d(TAG, "onSurfaceCreated - glDisable error: " + GLES20.glGetError());

        GLES20.glClearColor(0f, 0f, 0f, 1.0f);
        if(DEBUG_LOGGING) Log.d(TAG, "onSurfaceCreated - glClearColor error: " + GLES20.glGetError());

        // Creating the program
        int vshader = loadShader(GLES20.GL_VERTEX_SHADER, strVertexShader);
        int fshader = loadShader(GLES20.GL_FRAGMENT_SHADER, strFragmentShader);

        mProgram = GLES20.glCreateProgram();
        if(DEBUG_LOGGING) Log.d(TAG, "onSurfaceCreated - glCreateProgram error: " + GLES20.glGetError());

        GLES20.glAttachShader(mProgram, vshader);
        if(DEBUG_LOGGING) Log.d(TAG, "onSurfaceCreated - glAttachShader(vshader) error: " + GLES20.glGetError());

        GLES20.glAttachShader(mProgram, fshader);
        if(DEBUG_LOGGING) Log.d(TAG, "onSurfaceCreated - glAttachShader(fshader) error: " + GLES20.glGetError());

        GLES20.glLinkProgram(mProgram);
        if(DEBUG_LOGGING) Log.d(TAG, "onSurfaceCreated - glLinkProgram error: " + GLES20.glGetError());

        GLES20.glDetachShader(mProgram, fshader);
        GLES20.glDetachShader(mProgram, vshader);
        GLES20.glDeleteShader(fshader);
        GLES20.glDeleteShader(vshader);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        if(DEBUG_LOGGING) Log.d(TAG, "onSurfaceChanged - glViewport error: " + GLES20.glGetError());

        surfaceWidth = width;
        surfaceHeight = height;
    }

    private void start(){
        // xpos 0.0f
        // ypos -0.0f
        // 100.0f
        if (actualSourceHeight != 0 && actualSourceWidth != 0){
            VideoShaderHelper tempHelper = new VideoShaderHelper((float) actualSourceWidth,
                    (float) actualSourceHeight,
                    (float) surfaceWidth,
                    (float) surfaceHeight,
                    0.0f,
                    -0.0f,
                    100.0f,
                    VideoShaderHelper.FitMode.FIT_PAD);

            oglDisplayVertexShader = getShaderBuffer(tempHelper.getVertexShaderNormal());
            oglDisplayFragmentShader = getShaderBuffer(tempHelper.getFragmentShaderNormal());

            started = true;
        }

        initTexture();
    }

    private void initTexture() {
        if(DEBUG_LOGGING) Log.d(TAG, "initTexture");

        mTextureY = new int[1];
        mTextureU = new int[1];
        mTextureV = new int[1];

        GLES20.glGenTextures(1, mTextureY, 0);
        if(DEBUG_LOGGING) Log.d(TAG, "initTexture - glGenTextures0 error: "+GLES20.glGetError());
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureY[0]);
        if(DEBUG_LOGGING) Log.d(TAG, "initTexture - glBindTexture0 error: "+GLES20.glGetError());
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        if(DEBUG_LOGGING) Log.d(TAG, "initTexture - glTexParameteri1 error: "+GLES20.glGetError());
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        if(DEBUG_LOGGING) Log.d(TAG, "initTexture - glTexParameteri2 error: "+GLES20.glGetError());
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        if(DEBUG_LOGGING) Log.d(TAG, "initTexture - glTexParameteri3 error: "+GLES20.glGetError());
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        if(DEBUG_LOGGING) Log.d(TAG, "initTexture - glTexParameteri4 error: "+GLES20.glGetError());

        GLES20.glGenTextures(1, mTextureU, 0);
        if(DEBUG_LOGGING) Log.d(TAG, "initTexture - glGenTextures1 error: "+GLES20.glGetError());
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureU[0]);
        if(DEBUG_LOGGING) Log.d(TAG, "initTexture - glBindTexture1 error: "+GLES20.glGetError());
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glGenTextures(1, mTextureV, 0);
        if(DEBUG_LOGGING) Log.d(TAG, "initTexture - glGenTextures2 error: "+GLES20.glGetError());
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureV[0]);
        if(DEBUG_LOGGING) Log.d(TAG, "initTexture - glBindTexture2 error: "+GLES20.glGetError());
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
    }


    private FloatBuffer getShaderBuffer(float[] points) {
        if(DEBUG_LOGGING) Log.d(TAG, "getShaderBuffer");

        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            tmp.append(" ").append(points[i]);
        }
        if(DEBUG_LOGGING) Log.d(TAG, "getShaderBuffer points: "+tmp);

        FloatBuffer buffer = ByteBuffer.allocateDirect(points.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        buffer.put(points);
        buffer.position(0);

        return buffer;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glUseProgram(mProgram);

        synchronized (this) {
            if (!started) {
                start();
                return;
            }

            if ((null != yBuffer) && (null != uBuffer) && (null != vBuffer)) {
                int vPosition = GLES20.glGetAttribLocation(mProgram, "vPosition");
                if(DEBUG_LOGGING) Log.d(TAG, "onDrawFrame - glGetAttribLocation error: "+GLES20.glGetError());

                int vTexCoord = GLES20.glGetAttribLocation(mProgram, "vTexCoord");
                if(DEBUG_LOGGING) Log.d(TAG, "onDrawFrame - glGetAttribLocation error: "+GLES20.glGetError());

                int samplerY = GLES20.glGetUniformLocation(mProgram, "SamplerY");
                if(DEBUG_LOGGING) Log.d(TAG, "onDrawFrame - glGetUniformLocation error: "+GLES20.glGetError());

                int samplerU = GLES20.glGetUniformLocation(mProgram, "SamplerU");
                if(DEBUG_LOGGING) Log.d(TAG, "onDrawFrame - glGetUniformLocation error: "+GLES20.glGetError());

                int samplerV = GLES20.glGetUniformLocation(mProgram, "SamplerV");
                if(DEBUG_LOGGING) Log.d(TAG, "onDrawFrame - glGetUniformLocation error: "+GLES20.glGetError());

                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                if(DEBUG_LOGGING) Log.d(TAG, "onDrawFrame - glActiveTexture error: "+GLES20.glGetError());

                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureY[0]);
                if(DEBUG_LOGGING) Log.d(TAG, "onDrawFrame - glBindTexture error: "+GLES20.glGetError());


                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,
                        0,
                        GLES20.GL_LUMINANCE,
                        actualSourceWidth,
                        actualSourceHeight,
                        0,
                        GLES20.GL_LUMINANCE,
                        GLES20.GL_UNSIGNED_BYTE,
                        ByteBuffer.wrap(yBuffer));
                if(DEBUG_LOGGING) Log.d(TAG, "onDrawFrame - glTexImage2D GL_TEXTURE0 error: "+GLES20.glGetError());

                GLES20.glUniform1i(samplerY, 0);
                if(DEBUG_LOGGING) Log.d(TAG, "onDrawFrame - glUniform1i GL_TEXTURE0 error: "+GLES20.glGetError());

                GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
                if(DEBUG_LOGGING) Log.d(TAG, "onDrawFrame - glActiveTexture error: "+GLES20.glGetError());
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureU[0]);
                if(DEBUG_LOGGING) Log.d(TAG, "onDrawFrame - glBindTexture error: "+GLES20.glGetError());
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,
                        0,
                        GLES20.GL_LUMINANCE,
                        actualSourceWidth / 2,
                        actualSourceHeight / 2,
                        0,
                        GLES20.GL_LUMINANCE,
                        GLES20.GL_UNSIGNED_BYTE,
                        ByteBuffer.wrap(uBuffer));
                if(DEBUG_LOGGING) Log.d(TAG, "onDrawFrame - glTexImage2D GL_TEXTURE0 error: "+GLES20.glGetError());

                GLES20.glUniform1i(samplerU, 1);
                if(DEBUG_LOGGING) Log.d(TAG, "onDrawFrame - glUniform1i error: "+GLES20.glGetError());

                GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
                if(DEBUG_LOGGING) Log.d(TAG, "onDrawFrame - glActiveTexture error: "+GLES20.glGetError());
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureV[0]);
                if(DEBUG_LOGGING) Log.d(TAG, "onDrawFrame - glBindTexture error: "+GLES20.glGetError());
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,
                        0,
                        GLES20.GL_LUMINANCE,
                        actualSourceWidth / 2,
                        actualSourceHeight / 2,
                        0,
                        GLES20.GL_LUMINANCE,
                        GLES20.GL_UNSIGNED_BYTE,
                        ByteBuffer.wrap(vBuffer));
                GLES20.glUniform1i(samplerV, 2);
                if(DEBUG_LOGGING) Log.d(TAG, "onDrawFrame - glUniform1i error: "+GLES20.glGetError());

                GLES20.glEnableVertexAttribArray(vPosition);
                if(DEBUG_LOGGING) Log.d(TAG, "onDrawFrame - glEnableVertexAttribArray error: "+GLES20.glGetError());
                GLES20.glEnableVertexAttribArray(vTexCoord);
                if(DEBUG_LOGGING) Log.d(TAG, "onDrawFrame - glEnableVertexAttribArray error: "+GLES20.glGetError());

                GLES20.glVertexAttribPointer(vPosition,
                        2,
                        GLES20.GL_FLOAT,
                        true,
                        4 * 2,
                        oglDisplayVertexShader);
                GLES20.glVertexAttribPointer(vTexCoord,
                        2,
                        GLES20.GL_FLOAT,
                        true,
                        4 * 2,
                        oglDisplayFragmentShader);

                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
                if(DEBUG_LOGGING) Log.d(TAG, "onDrawFrame - glDrawArrays error: "+GLES20.glGetError());

                GLES20.glDisableVertexAttribArray(vPosition);
                if(DEBUG_LOGGING) Log.d(TAG, "onDrawFrame - glDisableVertexAttribArray error: "+GLES20.glGetError());
                GLES20.glDisableVertexAttribArray(vTexCoord);
                if(DEBUG_LOGGING) Log.d(TAG, "onDrawFrame - glDisableVertexAttribArray error: "+GLES20.glGetError());
            }
        }

        GLES20.glFlush();
    }

    private int loadShader(final int shaderType, final String shaderSource) {
        int shaderHandle = GLES20.glCreateShader(shaderType);

        if (shaderHandle != 0) {
            // Pass in the shader source.
            GLES20.glShaderSource(shaderHandle, shaderSource);

            // Compile the shader.
            GLES20.glCompileShader(shaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                if(DEBUG_LOGGING) Log.d(TAG, "loadShader - compile error: " + GLES20.glGetShaderInfoLog(shaderHandle));
                GLES20.glDeleteShader(shaderHandle);
                shaderHandle = 0;
            }
        }

        if (shaderHandle != 0 && DEBUG_LOGGING) Log.d(TAG, "loadShader - compiled success");

        return shaderHandle;
    }

    void renderI420YUV(byte[] bytes, int width, int height){
        synchronized (this){
            int yCount = width * height;
            int uAndVCount = yCount / 4;

            yBuffer = Arrays.copyOfRange(bytes, 0, yCount);
            uBuffer = Arrays.copyOfRange(bytes, yCount, yCount + uAndVCount);
            vBuffer = Arrays.copyOfRange(bytes, yCount + uAndVCount, yCount + (uAndVCount * 2));

            actualSourceHeight = height;
            actualSourceWidth = width;
        }
    }
}

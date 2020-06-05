package com.zoiper.zdk.android.demo.video;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.zoiper.zdk.Account;
import com.zoiper.zdk.Call;
import com.zoiper.zdk.Context;
import com.zoiper.zdk.EventHandlers.VideoRendererEventsHandler;
import com.zoiper.zdk.Types.VideoFrameFormat;
import com.zoiper.zdk.android.demo.MainActivity;
import com.zoiper.zdk.android.demo.R;
import com.zoiper.zdk.android.demo.base.BaseActivity;
import com.zoiper.zdk.android.demo.video.in.ZDKVideoSurfaceView;
import com.zoiper.zdk.android.demo.video.out.I420Helper;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


/**
 * InVideoCallActivity
 *
 * @since 31/01/2019
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class InVideoCallActivity extends BaseActivity {
    private static final String TAG = "VideoCall";

    private static final I420Helper.ImageDimensions CAPTURE_DIMENSIONS = new I420Helper.ImageDimensions(640, 480);

    // Multithreading
    private Handler bgHandler;

    // ZDK stuff
    private Context zdkContext;
    private Call call;

    // Camera stuff
    private CameraManager cameraManager;
    private ImageReader imageReader;

    // Image pre-processing
    private I420Helper i420Helper;

    // UI
    private TextView tvStatus;
    private TextView tvNetwork;
    private TextView tvOutProcessing;

    private SurfaceView svOut;
    private ZDKVideoSurfaceView svIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_in_video_call);

        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);

        initViews();
        initLifecycleObservers();
        initBackgroundHandler();
        initImageReader();
    }

    @Override
    public void onZDKLoaded() {
        zdkContext = getZdkContext();

        try {
            chooseCamera();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private class CustomImageReader implements ImageReader.OnImageAvailableListener {
        private byte[] bytes;
        private long last = -1;
        private int fps;
        private long start;
        private long secondDisp;
        private long thirdDisp;
        private long endDisp;

        @SuppressLint("SetTextI18n")
        private CustomImageReader() {
            bytes = new byte[CAPTURE_DIMENSIONS.I420YUVArraySize()];

            Handler handler = new Handler();
            int delay = 400; //milliseconds

            handler.postDelayed(new Runnable(){
                public void run(){
                    runOnUiThread(() -> tvOutProcessing.setText(
                            "Start: " + start +
                            "\nFps: " + fps +
                            "\nSecond: " + secondDisp +
                            "\nThird: " + (thirdDisp) +
                            "\nTotal: " + (endDisp)
                    ));
                    handler.postDelayed(this, delay);
                }
            }, delay);
        }

        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();
            if (image != null) {
                start = SystemClock.elapsedRealtime();
                fps = (int) (1000/(start-last));
                last = start;

                bytes = I420Helper.imageToI420ByteArray(image);
                long second = SystemClock.elapsedRealtime();

                bytes = i420Helper.straightenFrame(bytes);
                long third = SystemClock.elapsedRealtime();

                if(call != null) {
                    call.sendVideoFrame(bytes, bytes.length, VideoFrameFormat.YUV420p);
                }

                secondDisp = second-start;
                thirdDisp = third-second;
                endDisp = SystemClock.elapsedRealtime()-start;

                image.close();
            }
        }
    }

    private void initImageReader(){
        imageReader = ImageReader.newInstance(CAPTURE_DIMENSIONS.width,
                CAPTURE_DIMENSIONS.height, ImageFormat.YUV_420_888, 2);
        imageReader.setOnImageAvailableListener(new CustomImageReader(), bgHandler);
    }

    /**
     * Looks for number, accountID in the intent and if found,
     * get the account, create a call or find it and BIND listeners on it
     */
    private void bindCall(){
        Intent intent = getIntent();
        if (intent.hasExtra(MainActivity.INTENT_EXTRA_ACCOUNT_ID)
                && intent.hasExtra(MainActivity.INTENT_EXTRA_NUMBER)) {
            long accountId = intent.getLongExtra(MainActivity.INTENT_EXTRA_ACCOUNT_ID, -1);
            String number = intent.getStringExtra(MainActivity.INTENT_EXTRA_NUMBER);

            Account account = zdkContext.accountProvider().getAccount(accountId);

            List<Call> calls = queryActiveCalls(account);

            call = createOrGetCall(account, calls, number);

            call.setCallStatusListener(new VideoCallEventsHandler(this));

            call.setVideoRendererNotificationsListener(new VideoRendererEventsHandler() {
                @Override
                public void onVideoFrameReceived(byte[] pBuffer, int length, int width, int height) {
                    svIn.renderI420YUV(pBuffer, width, height);
                }
            });
        }
    }

    private List<Call> queryActiveCalls(Account account) {
        return account.getActiveCalls();
    }

    private Call createOrGetCall(Account account, List<Call> calls, String number){
        return calls.size() > 0 ? calls.get(0) : account.createCall(number, true, true);
    }

    private int deviceOrientation() {
        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        if (rotation == Surface.ROTATION_0) return 0;
        if (rotation == Surface.ROTATION_90) return 90;
        if (rotation == Surface.ROTATION_180) return 180;
        if (rotation == Surface.ROTATION_270) return 270;
        return -1;
    }

    private void initLifecycleObservers() {
        getLifecycle().addObserver(svIn);
    }

    private void initBackgroundHandler() {
        HandlerThread background = new HandlerThread("Background");
        background.start();
        bgHandler = new Handler(background.getLooper());
    }

    void printStatusThreadSafe(String status) {
        runOnUiThread(() -> {
            if (tvStatus != null) {
                tvStatus.setText(status);
            }
        });
    }

    void printNetworkThreadSafe(String status) {
        runOnUiThread(() -> {
            if (tvNetwork != null) {
                tvNetwork.setText(status);
            }
        });
    }

    void printGeneralThreadSafe(String text) {
        runOnUiThread(() -> {
            Log.i(TAG, text);
        });
    }

    private void initViews() {
        svIn = findViewById(R.id.video_call_sv_in);
        svOut = findViewById(R.id.video_call_sv_out);
        tvStatus = findViewById(R.id.video_call_tv_status);
        tvNetwork = findViewById(R.id.video_call_tv_network);
        tvOutProcessing = findViewById(R.id.video_call_tv_out_processing);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void chooseCamera() throws CameraAccessException {
        String[] cameraIdList = cameraManager.getCameraIdList();

        for (String cid : cameraIdList) {
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cid);

            Integer lensFacing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);

            if(lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_FRONT){
                Integer sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

                if(sensorOrientation != null){
                    initCamera(cid, sensorOrientation);
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void initCamera(String cameraId, Integer sensorOrientation) throws CameraAccessException {
        i420Helper = new I420Helper(sensorOrientation, deviceOrientation(), CAPTURE_DIMENSIONS);

        zdkContext.videoControls().setFormat(i420Helper.postRotateDimensions.width, i420Helper.postRotateDimensions.height, 30);

        cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NotNull CameraDevice camera) {
                try {
                    InVideoCallActivity.this.cameraStarted(camera);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDisconnected(@NotNull CameraDevice camera) {
                Toast.makeText(InVideoCallActivity.this, "Camera disconnected", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NotNull CameraDevice camera, int error) {
                Toast.makeText(InVideoCallActivity.this, "Camera error: " + error, Toast.LENGTH_SHORT).show();
            }
        }, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void cameraStarted(CameraDevice camera) throws CameraAccessException {
        ArrayList<Surface> surfaces = new ArrayList<>();

        surfaces.add(svOut.getHolder().getSurface());
        surfaces.add(imageReader.getSurface());

        camera.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                try {
                    captureSessionCreated(camera, session);
                } catch (CameraAccessException e) {
                    Toast.makeText(InVideoCallActivity.this, "Zdrkp", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            }
        }, bgHandler);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void captureSessionCreated(CameraDevice camera, CameraCaptureSession session) throws CameraAccessException {
        CaptureRequest.Builder crb = camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);

        crb.addTarget(svOut.getHolder().getSurface());
        crb.addTarget(imageReader.getSurface());

        CaptureRequest captureRequest = crb.build();

        session.setRepeatingRequest(captureRequest, null, bgHandler);

        bindCall();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (call != null) {
            if(isFinishing()){
                call.hangUp();
                Log.d(TAG, "onDestroy: hangUp()");
            }
            call.dropAllEventListeners();
            Log.d(TAG, "onDestroy: dropAllEventListeners()");
        }
    }
}

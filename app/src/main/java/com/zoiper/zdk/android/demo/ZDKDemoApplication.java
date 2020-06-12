package com.zoiper.zdk.android.demo;

import android.app.Application;
import android.os.Handler;
import android.widget.Toast;

import com.zoiper.zdk.ActivationResult;
import com.zoiper.zdk.Context;
import com.zoiper.zdk.EventHandlers.ContextEventsHandler;
import com.zoiper.zdk.Result;
import com.zoiper.zdk.SecureCertData;
import com.zoiper.zdk.Types.ActivationStatus;
import com.zoiper.zdk.Types.CertificateError;
import com.zoiper.zdk.Types.LoggingLevel;
import com.zoiper.zdk.Types.ResultCode;
import com.zoiper.zdk.Types.TLSSecureSuiteType;
import com.zoiper.zdk.android.demo.network.NetworkChangeReceiver;
import com.zoiper.zdk.android.demo.util.Credentials;

import java.io.File;

/**
 * ZDKDemoApplication
 *
 * This is where we instantiate and run the first configuration on our
 * {@link Context} object. It is highly recommended that you never repeat
 * this initialization if you don't explicitly know what you're doing.
 */
public class ZDKDemoApplication extends Application implements ContextEventsHandler {
    @SuppressWarnings("unused")
    private static final String TAG = "ZDKDemoApplication";

    private volatile boolean isZdkActivated = false;

    private Context zdkContext;
    private Handler mainHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        initializeZoiperContext();
        mainHandler = new Handler(getMainLooper());
    }

    public void initializeZoiperContext(){
        zdkContext = makeZoiperContext();
        zdkContext.setStatusListener(this);

        // Before we can use the instance, we need to activate it.
        new ActivatorThread().start();
    }

    private Context makeZoiperContext(){
        // We create the Context instance
        // NOTE: Keep this object single-instance
        try{
            Context zdkContext = new Context(getApplicationContext());

            File logFile = new File(getFilesDir(), "logs.txt");

            zdkContext.logger().logOpen(
                    logFile.getAbsolutePath(),
                    null,
                    LoggingLevel.Debug,
                    0
            );

            // Make sure you have both
            // ACCESS_NETWORK_STATE
            // and
            // INTERNET
            // permissions!!!!!!!!!!!
            zdkContext.configuration().sipPort(5060);
            //zdkContext.configuration().iaxPort();
            //zdkContext.configuration().rtpPort();

            zdkContext.configuration().enableSIPReliableProvisioning(false);
            zdkContext.encryptionConfiguration().tlsConfig().secureSuite(TLSSecureSuiteType.SSLv2_v3);
            zdkContext.encryptionConfiguration().globalZrtpCache(getZrtpCacheFile().getAbsolutePath());

            return zdkContext;
        } catch (UnsatisfiedLinkError e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method will get called when you have an error with your ZDK instance activation
     * and this is where you will get the reason for the error.
     *
     * !!!!!!!!!!!!!!!!DANGER!!!!!!!!!!!!!!
     * !!!!!!!!!!!SERIOUS NOTICE!!!!!!!!!!!
     * This method is CALLED ON THE ZDK THREAD.
     * Consider using mainHandler.post{} inside
     * it to execute code on the main thread
     */
    @Override
    public void onContextSecureCertStatus(Context context, SecureCertData secureCert) {
        // TODO("PLEASE IMPLEMENT ME ADEQUATELY!")
        if(secureCert.errorMask() != CertificateError.None.ordinal()) {
            mainHandler.post(() -> certificateError(secureCert));
        }
    }

    private void certificateError(SecureCertData secureCert) {
        Toast.makeText(this, "SecureCertError: expected= " + secureCert.expectedName() + ", got= " + secureCert.actualNameList(), Toast.LENGTH_LONG).show();

        // TODO("PLEASE IMPLEMENT ME ADEQUATELY!")
        // !!!!!!!!!!!SERIOUS NOTICE!!!!!!!!!!!
        // Do this ONLY after USER request!!!
        // The user should be warned that using exceptions makes TLS much less secure than they think it is.
        zdkContext.encryptionConfiguration().addKnownCertificate(secureCert.certDataPEM());
    }

    /**
     * !!!!!!!!!!!!!!!!DANGER!!!!!!!!!!!!!!
     * !!!!!!!!!!!SERIOUS NOTICE!!!!!!!!!!!
     *
     * This method is CALLED ON THE ZDK THREAD.
     * Consider using mainHandler.post{} inside
     * it to execute code on the main thread
     */
    @Override
    public void onContextActivationCompleted(Context context, ActivationResult activationResult) {
        if (activationResult.status() == ActivationStatus.Success) {
            mainHandler.post(this::activationSuccess);
        }
        else {
            String reason = activationResult.reason();
            mainHandler.post(() -> this.activationFailed(reason));
        }

        // ActivationStatus.Success
        // ActivationStatus.Unparsable
        // ActivationStatus.FailedDecrypt
        // ActivationStatus.Failed
        // ActivationStatus.FailedDeadline
        // ActivationStatus.FailedChecksum
        // ActivationStatus.FailedId
        // ActivationStatus.FailedCache
        // ActivationStatus.FailedHttp
        // ActivationStatus.FailedCurl
        // ActivationStatus.FailedSignCheck
        // ActivationStatus.Expired
    }

    private void activationFailed(String reason) {
        Toast.makeText(this, "Activation failed: "+reason, Toast.LENGTH_LONG).show();
    }

    private void activationSuccess() {
        Result startingResult = zdkContext.startContext();

        // If the ZDK Context activation process went ok,
        // we set isZdkActivated to true thus allowing all the waiting activities
        // use the context
        isZdkActivated = startingResult.code() == ResultCode.Ok;

        if(isZdkActivated){
            // After activation is complete, start notifying the ZDK context about network changes
            this.registerReceiver(
                    new NetworkChangeReceiver(zdkContext),
                    NetworkChangeReceiver.intentFilter()
            );
        }
    }

    /**
     * @return ZRTP cache file, put it where ever you please.
     * For our example we will put it in our android data directory
     */
    private File getZrtpCacheFile(){
        return new File(getFilesDir(), "cache_zrtp");
    }

    /**
     * @return Cert cache file, put it where ever you please.
     * For our example we will put it in our android data directory
     */
    private File getCertCacheFile(){
        return new File(getFilesDir(), "cache_cert");
    }

    public Context getZdkContext() {
        return zdkContext;
    }

    public Handler getMainHandler() { return mainHandler; }

    public boolean isZdkActivated() {
        return isZdkActivated;
    }

    /**
     * The activation process takes a bit more time,
     * thus we need a separate thread for it
     */
    private class ActivatorThread extends Thread{
        ActivatorThread() {
            super("ActivatorThread");
        }

        @Override
        public void run() {
            // Load the credentials.
            Credentials credentials = Credentials.load(getApplicationContext());

            // It is wise to offload that to a background thread because it takes some time
            zdkContext.activation().startSDK(
                    getCertCacheFile().getAbsolutePath(),
                    credentials.username,
                    credentials.password
            );
        }
    }
}

package com.zoiper.zdk.android.demo.base;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.zoiper.zdk.Account;
import com.zoiper.zdk.Context;
import com.zoiper.zdk.Providers.AccountProvider;
import com.zoiper.zdk.android.demo.ZDKDemoApplication;
import com.zoiper.zdk.android.demo.util.PermissionHelper;

/**
 * BaseActivity
 *
 * @since 30.1.2019 г.
 */
public abstract class BaseActivity extends AppCompatActivity {
    private PermissionHelper permissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check for the permissions and then listen for ZDK loaded
        permissionHelper = new PermissionHelper(this, this::listenForZdkContextLoaded);
    }

    /** Make every action bar have a navigate up arrow and a title */
    protected void setupActionbar(String title){
        final ActionBar supportActionBar = getSupportActionBar();
        if(supportActionBar == null) throw new RuntimeException("Run setSupportActionBar");
        supportActionBar.setDisplayHomeAsUpEnabled(true);
        supportActionBar.setTitle(title);
    }

    /**
     * Since you're not sure if the zdk context is activated when you might need it, we've
     * set up an example way for you to check and receive the context only when you're
     * 100% sure that it is in fact active and usable.
     * You would ideally want this in your BaseActivity
     */
    private void listenForZdkContextLoaded() {

        if (getMainApplication().isZdkActivated()){
            // Optimization - if zoiper was already loaded, skip spawning a new thread
            this.onZDKLoaded();
            return;
        }
        // Wait for the zdk context to get activated on a background thread
        new ZdkContextWaitThread().start();
    }

    /**
     * Get our application instance
     * Ideally this would be in your BaseActivity
     */
    protected ZDKDemoApplication getMainApplication() {
        return (ZDKDemoApplication) getApplication();
    }

    /**
     * Way to access your {@link Context} in your activity.
     * Ideally this would be in your BaseActivity
     */
    protected Context getZdkContext() {
        return getMainApplication().getZdkContext();
    }

    /**
     * Way to check if your {@link Context} is activated.
     * Ideally this would be in your BaseActivity
     */
    protected boolean isZdkActivated() {
        return getMainApplication().isZdkActivated();
    }

    /**
     * Gets the account associated with the ID.
     * @return The current account associated with the ID
     */
    public Account getAccount(long accountID) {
        AccountProvider accountProvider = getZdkContext().accountProvider();
        if (accountProvider != null) return accountProvider.getAccount(accountID);
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionHelper.onRequestPermissionsResult(requestCode);
    }

    /** Called when the ZDK context has successfully started */
    public void onZDKLoaded() {}

    /**
     * Called when the ZDK context fails to start in time.
     * The time is {@link ZdkContextWaitThread#ACTIVATION_TIMEOUT}
     */
    public void onZDKLoadTimeout() {}

    private class ZdkContextWaitThread extends Thread{
        private static final int ACTIVATION_TIMEOUT = 5 * 1000; // 5 seconds

        private ZdkContextWaitThread() {
            this.setName("ZdkContextWaitThread");
        }

        @Override
        public void run() {
            long startingPoint = System.currentTimeMillis();
            while (!isZdkActivated()) {
                try {
                    if ((System.currentTimeMillis() - startingPoint) > ACTIVATION_TIMEOUT) {
                        runOnUiThread(BaseActivity.this::onZDKLoadTimeout);
                        return;
                    }
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                    // In case the thread is interrupted before there is a result, notify failure
                    runOnUiThread(BaseActivity.this::onZDKLoadTimeout);
                }
            }
            // Execute successful callback
            runOnUiThread(BaseActivity.this::onZDKLoaded);
        }
    }

    public static String downloadsPath(){
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
    }

    public void delayedFinish(){ delayedFinish(1000); }
    @SuppressWarnings("SameParameterValue")
    public void delayedFinish(long delay){
        new Handler().postDelayed(this::finish, delay);
    }
}

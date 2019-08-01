package com.zoiper.zdk.android.demo.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

/**
 * PermissionHelper
 *
 * Just a helper class to make sure that all permissions are granted before we do anything.
 * Needless to say that this isn't an ideal way to handle permissions in real world use cases.
 *
 * @since 30.1.2019
 */
public class PermissionHelper {

    private static final int PERMISSIONS_REQUEST = 1;

    private Activity activity;
    private final PermissionReady permissionReadyListener;

    public interface PermissionReady {
        void onPermissionReady();
    }

    public PermissionHelper(Activity activity, PermissionReady permissionReadyListener) {
        this.activity = activity;
        this.permissionReadyListener = permissionReadyListener;

        if (!hasAllPermissions()) {
            askForPermissions();
        } else {
            onPermissionsAvailable();
        }
    }

    private boolean hasPermission(String permission) {
        int status = ContextCompat.checkSelfPermission(activity, permission);
        return status == PackageManager.PERMISSION_GRANTED;
    }

    private void askForPermissions() {
        ActivityCompat.requestPermissions(activity,
                                          new String[] {Manifest.permission.CAMERA,
                                                        Manifest.permission.CALL_PHONE,
                                                        Manifest.permission.RECORD_AUDIO,
                                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                          PERMISSIONS_REQUEST);
    }

    public void onRequestPermissionsResult(int requestCode) {
        if (requestCode == PERMISSIONS_REQUEST) {
            if (hasAllPermissions()) {
                onPermissionsAvailable();
            } else {
                Toast.makeText(activity, "You did not provide permissions", Toast.LENGTH_SHORT)
                     .show();
            }
            activity = null; // Important: release activity reference because memory leak otherwise
        }
    }

    private boolean hasAllPermissions() {
        return hasPermission(Manifest.permission.CAMERA) &&
               hasPermission(Manifest.permission.CALL_PHONE) &&
               hasPermission(Manifest.permission.RECORD_AUDIO) &&
               hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void onPermissionsAvailable() {
        if(permissionReadyListener == null) return;
        permissionReadyListener.onPermissionReady();
    }

}

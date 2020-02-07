package com.zoiper.zdk.android.demo.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.io.File;

public class LoggingUtils {

    private static final String DEBUG_LOG_EDITOR_KEY = "key_debug_log_filename";

    private static final String FILE_NAME = "logfile";

    public static String generateDebugLogFilename(Context context) {
        java.util.Date date = new java.util.Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("_yyyyMMdd_HHmmss");
        StringBuilder sb = new StringBuilder(sdf.format(date));
        String homeDirExternal = getHomeDirExternal();
        createDir(context, homeDirExternal);
        String filename = homeDirExternal +
                          FILE_NAME +
                          sb +
                          ".txt";
        saveDebugLogFilename(context, filename);
        return filename;
    }

    private static void createDir(Context context, String homeDirExternal) {
        File dir = new File(homeDirExternal);
        if (!dir.exists()) {
            boolean mkdir = dir.mkdir();
            if (!mkdir) {
                Toast.makeText(context,"Could not create folder",Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Application external home folder where various logs are stored.
     * It can be accessed via pc, from other apps and the user
     */
    private static String getHomeDirExternal() {
        return Environment.getExternalStorageDirectory().getPath() +"/zdk_demo/";
    }

    private static void saveDebugLogFilename(Context context, String filename) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(DEBUG_LOG_EDITOR_KEY, filename);

        editor.apply();
    }

}

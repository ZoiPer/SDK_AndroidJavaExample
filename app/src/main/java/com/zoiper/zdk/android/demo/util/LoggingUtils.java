package com.zoiper.zdk.android.demo.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileNotFoundException;

public class LoggingUtils {

    private static final String DEBUG_LOG_EDITOR_KEY = "key_debug_log_filename";

    private static final String FILE_NAME = "logfile";

    private static final String TRAILING_DASH = "/";

    public static String generateDebugLogFilename(Context context) throws FileNotFoundException {
        java.util.Date date = new java.util.Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("_yyyyMMdd_HHmmss");
        StringBuilder sb = new StringBuilder(sdf.format(date));
        String homeDirExternal = getHomeDirExternal(context);
        String filename = homeDirExternal +
                          TRAILING_DASH +
                          FILE_NAME +
                          sb +
                          ".txt";
        saveDebugLogFilename(context, filename);
        return filename;
    }

    private static String getHomeDirExternal(Context context) throws FileNotFoundException {
        File dir = context.getExternalFilesDir(null);
        if (dir == null) {
            throw new FileNotFoundException("Unable to access application external directory.");
        }
        return dir.getPath();
    }

    private static void saveDebugLogFilename(Context context, String filename) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(DEBUG_LOG_EDITOR_KEY, filename);

        editor.apply();
    }

}

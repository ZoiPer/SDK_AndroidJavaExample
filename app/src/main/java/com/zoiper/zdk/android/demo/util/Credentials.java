package com.zoiper.zdk.android.demo.util;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * Credentials
 *
 * @since 22.1.2019 г.
 */
public class Credentials {
    private static final String CREDENTIALS_FILE_NAME = "credentials.json";

    public final String username;
    public final String password;

    private Credentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    private static String loadJSONFromAsset(Context context) {
        String json;
        try {
            InputStream is = context.getAssets().open(CREDENTIALS_FILE_NAME);
            int size = is.available();
            byte[] buffer = new byte[size];
            //noinspection ResultOfMethodCallIgnored
            is.read(buffer);
            is.close();
            //noinspection CharsetObjectCanBeUsed
            json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            throw new IllegalArgumentException("Credentials file missing in assets/" + CREDENTIALS_FILE_NAME, e);
        }
        return json;
    }

    public static Credentials load(Context context){
        String jsonString = loadJSONFromAsset(context);
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return new Credentials(
                    jsonObject.getString("username"),
                    jsonObject.getString("password")
            );
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse json file :/", e);
        }
    }
}

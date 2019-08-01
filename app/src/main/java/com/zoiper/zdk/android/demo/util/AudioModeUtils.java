package com.zoiper.zdk.android.demo.util;

import android.content.Context;
import android.media.AudioManager;

/**
 * AudioModeUtils
 *
 * @since 4.2.2019 г.
 */
public class AudioModeUtils {

    /**
     * Set the device audio mode.
     *
     * @param mode
     *         The audio mode to be set. Can be:
     *         ({@link AudioManager#MODE_NORMAL}, {@link AudioManager#MODE_RINGTONE},
     *         {@link AudioManager#MODE_IN_CALL} or {@link AudioManager#MODE_IN_COMMUNICATION}).
     */
    public static void setAudioMode(Context context, int mode) {
        AudioManager audioManager =
                (AudioManager) context.getSystemService(android.content.Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setMode(mode);
        }
    }

}

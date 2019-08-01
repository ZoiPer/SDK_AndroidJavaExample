package com.zoiper.zdk.android.demo.util;

import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.Timer;
import java.util.TimerTask;

/**
 * DurationTimer
 *
 * @since 1.2.2019 г.
 */
public class DurationTimer {
    private static final int DELAY = 0;
    private static final int PERIOD = 1000;

    // Seconds since the timer was first started
    private int secondsSinceStart = 0;

    // Java util timer
    private Timer durationTimer;

    private final TimerChangeListener changeListener;
    private final Handler callbackHandler;

    /**
     * Listener as last parameter to make this class Kotlin-lambda-friendly
     *
     * @param callbackHandler All {@link TimerChangeListener#onTimerChange} events will be fired
     *                         through this executor
     */
    public DurationTimer(Handler callbackHandler,
                         @NonNull TimerChangeListener changeListener) {
        this.changeListener = changeListener;
        this.callbackHandler = callbackHandler;
    }

    /**
     * Cancels the timer, doesn't update the
     * {@link TimerChangeListener} about the cancellation
     */
    public void cancel() {
        if(durationTimer != null){
            durationTimer.cancel();
            durationTimer = null;
        }
    }

    /**
     * Starts the timer, either from 0 or from where it left off
     */
    public void start() {
        durationTimer = new Timer();
        durationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                String durationFormatted = secondsToText(secondsSinceStart);
                callbackHandler.post(() -> changeListener.onTimerChange(durationFormatted));

                secondsSinceStart++;
            }
        }, DELAY, PERIOD);
    }


    /**
     * Resets the timer back down to 0 and update the {@link TimerChangeListener}.
     * This doesn't stop the timer if it's already running.
     */
    public void reset(){
        secondsSinceStart = 0;
        String durationFormatted = secondsToText(secondsSinceStart);
        callbackHandler.post(() -> changeListener.onTimerChange(durationFormatted));
    }

    private String secondsToText(int seconds) {
        int secondsLeft = seconds % 3600 % 60;
        int minutes = (int) Math.floor(seconds % 3600D / 60D);

        String MM = minutes < 10 ? "0" + minutes : String.valueOf(minutes);
        String SS = secondsLeft < 10 ? "0" + secondsLeft : String.valueOf(secondsLeft);

        return MM + ":" + SS;
    }

    public interface TimerChangeListener{
        void onTimerChange(String time);
    }
}

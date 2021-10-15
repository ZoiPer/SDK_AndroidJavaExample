package com.zoiper.zdk.android.demo.call;

import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zoiper.zdk.Account;
import com.zoiper.zdk.Call;
import com.zoiper.zdk.CallStatus;
import com.zoiper.zdk.EventHandlers.CallEventsHandler;
import com.zoiper.zdk.ExtendedError;
import com.zoiper.zdk.NetworkStatistics;
import com.zoiper.zdk.Result;
import com.zoiper.zdk.Types.CallLineStatus;
import com.zoiper.zdk.Types.CallMediaChannel;
import com.zoiper.zdk.Types.CallSecurityLevel;
import com.zoiper.zdk.Types.NetworkQualityLevel;
import com.zoiper.zdk.Types.ResultCode;
import com.zoiper.zdk.Types.Zrtp.ZRTPAuthTag;
import com.zoiper.zdk.Types.Zrtp.ZRTPCipherAlgorithm;
import com.zoiper.zdk.Types.Zrtp.ZRTPHashAlgorithm;
import com.zoiper.zdk.Types.Zrtp.ZRTPKeyAgreement;
import com.zoiper.zdk.Types.Zrtp.ZRTPSASEncoding;
import com.zoiper.zdk.android.demo.MainActivity;
import com.zoiper.zdk.android.demo.R;
import com.zoiper.zdk.android.demo.base.BaseActivity;
import com.zoiper.zdk.android.demo.util.AudioModeUtils;
import com.zoiper.zdk.android.demo.util.DialogUtil;
import com.zoiper.zdk.android.demo.util.DurationTimer;
import com.zoiper.zdk.android.demo.util.TextViewSelectionUtils;

public class InCallActivity extends BaseActivity implements CallEventsHandler {

    private static final String TAG = "InCallActivity";

    // Config
    private String recordingFilePath = downloadsPath();

    // ZDK
    private Account account;
    private Call call;

    // UI
    private TextView callStateTextView;
    private TextView timerTextView;

    // Util
    private DurationTimer durationTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_call);
        setSupportActionBar(findViewById(R.id.toolbar));

        setupActionbar("In call");
        setupViews();

        // Create the duration timer and hook it up to the TextView
        durationTimer = new DurationTimer(
                getMainApplication().getMainHandler(),
                timerTextView::setText
        );
    }

    @Override
    public void onZDKLoaded() {
        long accountID = getIntent().getLongExtra(MainActivity.INTENT_EXTRA_ACCOUNT_ID, 0);
        String number = getIntent().getStringExtra(MainActivity.INTENT_EXTRA_NUMBER);

        account = getAccount(accountID);

        if(number != null && account != null) createCall(number);
    }

    private void createCall(String number){
        call = account.createCall(number, true, false);

        // If the call is created successfully we should set the Audio mode to Communication and
        // set the call status listener to change back the audio mode to Normal when the call is finished.
        if (call != null) {
            AudioModeUtils.setAudioMode(this,AudioManager.MODE_IN_COMMUNICATION);
            call.setCallStatusListener(this);
        } else {
            setStatus("Can't create the call");
        }
    }

    private void setupViews() {
        timerTextView = findViewById(R.id.call_timer_textview);
        callStateTextView = findViewById(R.id.call_state_textview);

        // Floating action button to hangup
        findViewById(R.id.fab).setOnClickListener(view -> hangup());

        // Toggle buttons
        findViewById(R.id.in_call_mute_button)
                .setOnClickListener(view -> muteCall(view, view.isSelected()));

        findViewById(R.id.in_call_hold_button)
                .setOnClickListener(view -> holdCall(view, view.isSelected()));

        findViewById(R.id.in_call_record_button)
                .setOnClickListener(view -> recordCall(view, view.isSelected()));

        // Transfer
        findViewById(R.id.in_call_transfer_button).setOnClickListener(view -> promptTransfer());
    }

    private void promptForIncomingTransfer(String name, String number) {
        DialogUtil.promptAgreement(
                this,
                "Accept call transfer? " + name + " (" + number + ")",
                "Accept",
                "Reject",
                this::acceptCallTransfer,
                this::rejectCallTransfer
        );
    }

    private void promptTransfer() {
        DialogUtil.promptText(
                this,
                "Transfer call to: ",
                "Transfer",
                "Cancel",
                "Enter number",
                this::transferCall,
                null
        );
    }

    private void rejectCallTransfer() {
        if(call == null) return;
        call.rejectCallTransfer();
        Toast.makeText(this, "Call transfer rejected", Toast.LENGTH_LONG).show();
    }

    private void acceptCallTransfer() {
        if(call == null) return;

        Call transferedCall = call.acceptCallTransfer();
        if(transferedCall != null)
        {
            call = transferedCall;
            Toast.makeText(this, "Call transfer accepted", Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(this, "Accepting call transfer FAILED!", Toast.LENGTH_LONG).show();
        }
    }


    private void transferCall(String number) {
        if(call == null) return;
        call.blindTransfer(number);
    }

    private void recordCall(View view, boolean selected) {
        boolean newSelectedState = !selected;
        TextViewSelectionUtils.setTextViewSelected((TextView) view, newSelectedState);
        if (newSelectedState) {
            // The filename should probably be callee name/number for usability sake.
            call.recordFileName(recordingFilePath + "/zdk_record_" + System.currentTimeMillis() + ".wav");
            call.startRecording();
            Toast.makeText(this, "Recording call", Toast.LENGTH_LONG).show();
        } else {
            call.stopRecording();
            Toast.makeText(this, "Recording saved to " + call.recordFileName() , Toast.LENGTH_LONG).show();
        }
    }

    private void holdCall(View view, boolean selected) {
        boolean newSelectedState = !selected;
        call.held(newSelectedState);
        TextViewSelectionUtils.setTextViewSelected((TextView) view, newSelectedState);
    }

    private void muteCall(View view, boolean selected) {
        boolean newSelectedState = !selected;
        call.muted(newSelectedState);
        TextViewSelectionUtils.setTextViewSelected((TextView) view, newSelectedState);
    }

    /**
     * Hangup the call. If the call is already terminated, finish the activity.
     */
    private void hangup() {
        if (call!=null && call.status().lineStatus() != CallLineStatus.Terminated) {
            //noinspection unused
            Result hangUpResult = call.hangUp();
        }
    }

    private void setStatus(String status) {
        if (callStateTextView != null) {
            callStateTextView.setText(status);
        }
    }

    /**
     * @param status Important note! Don't use this argument outside of the callback
     *               (e.g. on another thread etc.) as it's destroyed on a native level
     *               right after the callback returns.
     */
    @Override
    public void onCallStatusChanged(Call call, CallStatus status) {
        // Notice how we first get the lineStatus and make it a jvm-managed string
        String lineStatus = status.lineStatus().toString();

        // And then use the string on the other thread, not the CallStatus
        runOnUiThread(() -> setStatus(lineStatus));

        // Set the audio mode to Normal after the call is terminated.
        if (status.lineStatus() == CallLineStatus.Terminated) {
            runOnUiThread(() -> {
                AudioModeUtils.setAudioMode(this,AudioManager.MODE_NORMAL);
                durationTimer.cancel();
                delayedFinish();
            });
        }
        if (status.lineStatus() == CallLineStatus.Active) {
            runOnUiThread(() -> durationTimer.start());
        }
    }

    @Override
    public void onCallExtendedError(Call call, ExtendedError error) {

    }

    @Override
    public void onCallNetworkStatistics(Call call, NetworkStatistics networkStatistics) {

    }

    @Override
    public void onCallNetworkQualityLevel(Call call, int callChannel, NetworkQualityLevel qualityLevel) {

    }

    @Override
    public void onCallTransferSucceeded(Call call) {
        runOnUiThread(() -> setStatus("Transfer succeeded"));
    }

    @Override
    public void onCallTransferFailure(Call call, ExtendedError error) {
        runOnUiThread(() -> setStatus("Transfer failed: " + error.message()));
    }

    @Override
    public void onCallTransferStarted(Call call, String name, String number, String uri) {
        runOnUiThread(() -> {
            setStatus("Transfer started: " + name + " (" + number + ")");
            promptForIncomingTransfer(name, number);
        });
    }

    @Override
    public void onCallZrtpFailed(Call call, ExtendedError error) {
        Log.d(TAG, "onCallZrtpFailed: call= " + call.callHandle() + "; error= " + error.message());
    }

    @Override
    public void onCallZrtpSuccess(Call call, String zidHex, int knownPeer, int cacheMismatch, int peerKnowsUs, ZRTPSASEncoding zrtpsasEncoding, String sas,
                                  ZRTPHashAlgorithm zrtpHashAlgorithm, ZRTPCipherAlgorithm zrtpCipherAlgorithm, ZRTPAuthTag zrtpAuthTag, ZRTPKeyAgreement zrtpKeyAgreement) {
        Log.d(TAG, "onCallZrtpSuccess: call= " + call.callHandle());

        if ((knownPeer != 0) && (cacheMismatch == 0) && (peerKnowsUs != 0))
        {
            runOnUiThread(() -> call.confirmZrtpSas(true));
        }
        else
        {
            runOnUiThread(() -> new AlertDialog.Builder(this)
                    .setTitle("SAS Verification")
                    .setMessage("SAS Verification is \"" + sas + "\". Please compare the string with your peer!")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            call.confirmZrtpSas(true);
                        }
                    })
                    .setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            call.confirmZrtpSas(false);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show());
        }
    }

    @Override
    public void onCallZrtpSecondaryError(Call call, int channel, ExtendedError error) {
        Log.d(TAG, "onCallZrtpSecondaryError: call= " + call.callHandle() + "; error= " + error.message());
    }

    @Override
    public void onCallSecurityLevelChanged(Call call, CallMediaChannel channel, CallSecurityLevel level) {
        Log.d(TAG,"OnCallSecurityLevelChanged channel: " + channel.toString() + " level: " + level.toString());
    }
}

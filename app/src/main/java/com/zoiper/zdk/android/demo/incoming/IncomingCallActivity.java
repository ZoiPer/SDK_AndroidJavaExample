package com.zoiper.zdk.android.demo.incoming;

import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zoiper.zdk.Account;
import com.zoiper.zdk.Call;
import com.zoiper.zdk.CallStatus;
import com.zoiper.zdk.EventHandlers.AccountEventsHandler;
import com.zoiper.zdk.EventHandlers.CallEventsHandler;
import com.zoiper.zdk.ExtendedError;
import com.zoiper.zdk.NetworkStatistics;
import com.zoiper.zdk.Types.AccountStatus;
import com.zoiper.zdk.Types.CallLineStatus;
import com.zoiper.zdk.Types.OwnershipChange;
import com.zoiper.zdk.Types.Zrtp.ZRTPAuthTag;
import com.zoiper.zdk.Types.Zrtp.ZRTPCipherAlgorithm;
import com.zoiper.zdk.Types.Zrtp.ZRTPHashAlgorithm;
import com.zoiper.zdk.Types.Zrtp.ZRTPKeyAgreement;
import com.zoiper.zdk.Types.Zrtp.ZRTPSASEncoding;
import com.zoiper.zdk.android.demo.MainActivity;
import com.zoiper.zdk.android.demo.R;
import com.zoiper.zdk.android.demo.base.BaseActivity;
import com.zoiper.zdk.android.demo.util.AudioModeUtils;
import com.zoiper.zdk.android.demo.util.TextViewSelectionUtils;

import java.util.List;

/**
 * IncomingCallActivity
 *
 * @since 1.2.2019 г.
 */
public class IncomingCallActivity extends BaseActivity
        implements AccountEventsHandler, CallEventsHandler {

    // UI
    private View incomingCallLayoutBase;

    private TextView incomingFromTextView;

    private TextView waitingTextView;

    private TextView statusTextView;

    private TextView speakerButton;

    private TextView muteButton;

    private ImageButton answerButton;

    private ImageButton hangupButton;

    // ZDK
    private Account account;

    // Android
    private Ringtone ringtone;

    private long currentCallHandle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);
        setSupportActionBar(findViewById(R.id.toolbar));

        setupActionbar("Incoming call");
        setupViews();
    }

    @Override
    public void onZDKLoaded() {
        long accountId = getIntent().getLongExtra(MainActivity.INTENT_EXTRA_ACCOUNT_ID, 0);
        account = getAccount(accountId);
        if (account != null) {
            account.setStatusEventListener(this);
        }
    }

    private void setupViews() {
        incomingCallLayoutBase = findViewById(R.id.incoming_call_layout_base);

        statusTextView = findViewById(R.id.incoming_call_status);
        waitingTextView = findViewById(R.id.waiting_textview);
        incomingFromTextView = findViewById(R.id.incoming_from_textview);

        // Action buttons
        answerButton = findViewById(R.id.button_answer_call);
        hangupButton = findViewById(R.id.button_hangup);

        //Toggle buttons
        speakerButton = findViewById(R.id.speaker_button);
        speakerButton.setSelected(false);
        muteButton = findViewById(R.id.mute_button);
        muteButton.setSelected(false);
    }

    private void setupIncomingCallView() {
        waitingTextView.setVisibility(View.GONE);
        incomingCallLayoutBase.setVisibility(View.VISIBLE);

        speakerButton.setOnClickListener(v -> onSpeakerButtonClicked(v.isSelected()));
        muteButton.setOnClickListener(v -> onMuteButtonClicked(v.isSelected()));

        answerButton.setOnClickListener(v -> onAnswerButtonClicked());
        hangupButton.setOnClickListener(v -> onHangupButtonClicked());
    }

    private void onHangupButtonClicked() {
        Call currentCall = getCallByHandle(currentCallHandle);
        if (currentCall != null) {
            currentCall.hangUp();
            AudioModeUtils.setAudioMode(this, AudioManager.MODE_NORMAL);
        }
    }

    private void onAnswerButtonClicked() {
        Call currentCall = getCallByHandle(currentCallHandle);
        if (currentCall != null) {
            currentCall.acceptCall();
        }
    }

    private void onMuteButtonClicked(boolean selected) {
        Call currentCall = getCallByHandle(currentCallHandle);
        if (currentCall != null) {
            boolean newSelectedState = !selected;
            currentCall.muted(newSelectedState);
            TextViewSelectionUtils.setTextViewSelected(muteButton, newSelectedState);
        }
    }

    private void onSpeakerButtonClicked(boolean selected) {
        Call currentCall = getCallByHandle(currentCallHandle);
        if (currentCall != null) {
            boolean newSelectedState = !selected;
            currentCall.onSpeaker(newSelectedState);
            TextViewSelectionUtils.setTextViewSelected(speakerButton, newSelectedState);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (account != null) {
            account.dropStatusEventListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (account != null) {
            account.setStatusEventListener(this);
        }
    }

    private Call getCallByHandle(long callHandle) {
        List<Call> calls = getZdkContext().callsProvider().calls();
        for (Call call : calls) {
            if (call.callHandle() == callHandle) {
                return call;
            }
        }
        return null;
    }

    private void onIncomingCall(long callHandle) {
        ring();

        statusTextView.setText(getString(R.string.ringing));

        Call currentCall = getCallByHandle(callHandle);
        currentCall.setCallStatusListener(this);

        setupIncomingCallView();

        // Set caller name.
        String incomingName = getString(R.string.incoming_call_from) +
                              "\n" +
                              currentCall.calleeName() +
                              " (" +
                              currentCall.calleeNumber() +
                              ")";

        incomingFromTextView.setText(incomingName);
        AudioModeUtils.setAudioMode(this, AudioManager.MODE_IN_COMMUNICATION);

    }

    private void ring() {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone = RingtoneManager.getRingtone(this, uri);
        ringtone.play();
    }

    @Override
    public void onAccountStatusChanged(Account account, AccountStatus accountStatus, int i) {

    }

    @Override
    public void onAccountRetryingRegistration(Account account, int i, int i1) {

    }

    @Override
    public void onAccountIncomingCall(Account account, Call call) {
        currentCallHandle = call.callHandle();
        runOnUiThread(() -> onIncomingCall(currentCallHandle));
    }

    @Override
    public void onAccountChatMessageReceived(Account account, String s, String s1) {

    }

    @Override
    public void onAccountExtendedError(Account account, ExtendedError extendedError) {

    }

    @Override
    public void onAccountUserSipOutboundMissing(Account account) {

    }

    @Override
    public void onAccountCallOwnershipChanged(Account account,
                                              Call call,
                                              OwnershipChange ownershipChange) {

    }

    @Override
    public void onCallStatusChanged(Call call, CallStatus callStatus) {
        String lineStatus = callStatus.lineStatus().toString();
        runOnUiThread(() -> statusTextView.setText(lineStatus));
        if (!callStatus.lineStatus().equals(CallLineStatus.Ringing)) {
            runOnUiThread(() -> ringtone.stop());
        }
        if (callStatus.lineStatus().equals(CallLineStatus.Terminated)) {
            getCallByHandle(currentCallHandle).dropCallStatusListener(this);
            runOnUiThread(this::delayedFinish);
        }
    }

    @Override
    public void onCallExtendedError(Call call, ExtendedError extendedError) {

    }

    @Override
    public void onCallNetworkStatistics(Call call, NetworkStatistics networkStatistics) {

    }

    @Override
    public void onCallNetworkQualityLevel(Call call, int i, int i1) {

    }

    @Override
    public void onCallTransferSucceeded(Call call) {

    }

    @Override
    public void onCallTransferFailure(Call call, ExtendedError extendedError) {

    }

    @Override
    public void onCallTransferStarted(Call call, String s, String s1, String s2) {

    }

    @Override
    public void onCallZrtpFailed(Call call, ExtendedError extendedError) {

    }

    @Override
    public void onCallZrtpSuccess(Call call,
                                  String s,
                                  int i,
                                  int i1,
                                  int i2,
                                  ZRTPSASEncoding zrtpsasEncoding,
                                  String s1,
                                  ZRTPHashAlgorithm zrtpHashAlgorithm,
                                  ZRTPCipherAlgorithm zrtpCipherAlgorithm,
                                  ZRTPAuthTag zrtpAuthTag,
                                  ZRTPKeyAgreement zrtpKeyAgreement) {

    }

    @Override
    public void onCallZrtpSecondaryError(Call call, int i, ExtendedError extendedError) {

    }
}

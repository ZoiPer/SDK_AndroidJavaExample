package com.zoiper.zdk.android.demo.dtmf;

import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.zoiper.zdk.Account;
import com.zoiper.zdk.Call;
import com.zoiper.zdk.CallStatus;
import com.zoiper.zdk.Configurations.AccountConfig;
import com.zoiper.zdk.EventHandlers.AccountEventsHandler;
import com.zoiper.zdk.EventHandlers.CallEventsHandler;
import com.zoiper.zdk.ExtendedError;
import com.zoiper.zdk.NetworkStatistics;
import com.zoiper.zdk.Providers.AccountProvider;
import com.zoiper.zdk.Result;
import com.zoiper.zdk.Types.AccountStatus;
import com.zoiper.zdk.Types.AudioVideoCodecs;
import com.zoiper.zdk.Types.CallLineStatus;
import com.zoiper.zdk.Types.DTMFCodes;
import com.zoiper.zdk.Types.DTMFTypeSIP;
import com.zoiper.zdk.Types.OwnershipChange;
import com.zoiper.zdk.Types.ProtocolType;
import com.zoiper.zdk.Types.RPortType;
import com.zoiper.zdk.Types.TransportType;
import com.zoiper.zdk.Types.Zrtp.ZRTPAuthTag;
import com.zoiper.zdk.Types.Zrtp.ZRTPCipherAlgorithm;
import com.zoiper.zdk.Types.Zrtp.ZRTPHashAlgorithm;
import com.zoiper.zdk.Types.Zrtp.ZRTPKeyAgreement;
import com.zoiper.zdk.Types.Zrtp.ZRTPSASEncoding;
import com.zoiper.zdk.android.demo.MainActivity;
import com.zoiper.zdk.android.demo.R;
import com.zoiper.zdk.android.demo.base.BaseActivity;
import com.zoiper.zdk.android.demo.util.AudioModeUtils;

import java.util.ArrayList;
import java.util.List;

public class DTMFActivity extends BaseActivity implements CallEventsHandler, AccountEventsHandler {

    public static final DTMFTypeSIP DTMF_TYPE = DTMFTypeSIP.SIP_info_numeric;

    // Login details to a DTMF-capable server
    public static final String USERNAME = setMe();
    public static final String PASSWORD = setMe();
    public static final String DOMAIN = setMe();

    private static String setMe(){
        throw new RuntimeException("Please set me!!");
    }

    // UI
    private EditText numberEditText;
    private TextView callStateTextView;

    // ZDK
    private Account account;
    private Call call;

    // Parameters
    private String number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dtmf);
        setSupportActionBar(findViewById(R.id.toolbar));

        setupActionbar(getString(R.string.dtmf));

        setupViews();
    }

    @Override
    public void onZDKLoaded() {
        number = getIntent().getStringExtra(MainActivity.INTENT_EXTRA_NUMBER);
        // Setup account for DTMF
        setupAccount();

        //noinspection unused
        Result createUserResult = account.createUser();
        Result registerAccountResult = account.registerAccount();
        setStatus(registerAccountResult.text());
    }

    private void startCall() {
        if(call != null) return;

        call = account.createCall(number, true, false);

        // If the call is created successfully we should set the Audio mode to Communication and
        // set the call status listener to change back the audio mode to Normal when the call is finished.
        if (call != null) {
            AudioModeUtils.setAudioMode(this, AudioManager.MODE_IN_COMMUNICATION);
            call.setCallStatusListener(this);
        }
    }

    private void setupAccount() {
        AccountProvider accountProvider = getZdkContext().accountProvider();
        account = accountProvider.createUserAccount();
        account.accountName(USERNAME);
        // Always set account configuration like this, account.configuration() returns copy of the object
        // and everything you set in it, wont matter.
        AccountConfig accountConfig = accountProvider.createAccountConfiguration();

        accountConfig.userName(USERNAME);
        accountConfig.password(PASSWORD);
        accountConfig.type(ProtocolType.SIP);

        accountConfig.sip(accountProvider.createSIPConfiguration());

        accountConfig.sip().transport(TransportType.UDP);

        accountConfig.sip().domain(DOMAIN);
        accountConfig.sip().dtmf(DTMF_TYPE);

        accountConfig.sip().rPort(RPortType.SignalingAndMedia);

        accountConfig.reregistrationTime(60);


        account.mediaCodecs(getAudioCodecs());
        account.setStatusEventListener(this);
        account.configuration(accountConfig);
    }

    /**
     * Returns a List with all the Audio codecs.
     *
     * @return List with audio codecs.
     */
    @NonNull
    private List<AudioVideoCodecs> getAudioCodecs() {
        List<AudioVideoCodecs> codecs = new ArrayList<>();
        codecs.add(AudioVideoCodecs.OPUS_WIDE);
        codecs.add(AudioVideoCodecs.PCMU);
        codecs.add(AudioVideoCodecs.vp8); // This is for the videocall
        return codecs;
    }

    private void setupViews() {
        numberEditText = findViewById(R.id.number_edit_text);
        callStateTextView = findViewById(R.id.status_textview);

        // Hangup
        findViewById(R.id.hangup_fab).setOnClickListener(v -> hangup());

        // Dialpad
        findViewById(R.id.buttonDelete).setOnClickListener(v -> deleteDigit());
        findViewById(R.id.buttonStar).setOnClickListener(v -> enterDigit("*"));
        findViewById(R.id.buttonHash).setOnClickListener(v -> enterDigit("#"));
        findViewById(R.id.button0).setOnClickListener(v -> enterDigit("0"));
        findViewById(R.id.button1).setOnClickListener(v -> enterDigit("1"));
        findViewById(R.id.button2).setOnClickListener(v -> enterDigit("2"));
        findViewById(R.id.button3).setOnClickListener(v -> enterDigit("3"));
        findViewById(R.id.button4).setOnClickListener(v -> enterDigit("4"));
        findViewById(R.id.button5).setOnClickListener(v -> enterDigit("5"));
        findViewById(R.id.button6).setOnClickListener(v -> enterDigit("6"));
        findViewById(R.id.button7).setOnClickListener(v -> enterDigit("7"));
        findViewById(R.id.button8).setOnClickListener(v -> enterDigit("8"));

        findViewById(R.id.button9).setOnClickListener(v -> enterDigit("9"));
    }

    /**
     * Hangup the call. If the call is already terminated, finish the activity.
     */
    private void hangup() {
        if (call != null && call.status().lineStatus() != CallLineStatus.Terminated) {
            //noinspection unused
            Result hangup = call.hangUp();
        }
    }

    private void deleteDigit() {
        int length = numberEditText.getText().length();
        if (length > 0) {
            numberEditText.getText().delete(length - 1, length);
        }
    }

    private void enterDigit(String digit) {
        numberEditText.append(digit);
        try {
            int digitInteger = Integer.valueOf(digit);
            if (call != null) {
                call.playDTMFSound(DTMFCodes.fromInt(digitInteger));
                call.sendDTMF(DTMFCodes.fromInt(digitInteger));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
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
        Log.d("Test", "onCallStatusChanged: " + status.lineStatus().toString());
        // Set the call status with handler.
        //setStatusWithHandler(status.lineStatus().toString());

        // Set the audio mode to Normal after the call is terminated.
        if (status.lineStatus() == CallLineStatus.Terminated) {
            runOnUiThread(() -> AudioModeUtils.setAudioMode(this, AudioManager.MODE_NORMAL));
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

    /**
     * @param accountStatus Important note! Don't use this argument outside of the callback
     *                      (e.g. on another thread etc.) as it's destroyed on a native level
     *                      right after the callback returns.
     */
    @Override
    public void onAccountStatusChanged(Account account, AccountStatus accountStatus, int i) {
        final String as = accountStatus.toString();

        runOnUiThread(this::startCall);
        runOnUiThread(() -> setStatus(as + " code: " + i));
    }

    @Override
    public void onAccountRetryingRegistration(Account account, int i, int i1) {

    }

    @Override
    public void onAccountIncomingCall(Account account, Call call) {

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
}

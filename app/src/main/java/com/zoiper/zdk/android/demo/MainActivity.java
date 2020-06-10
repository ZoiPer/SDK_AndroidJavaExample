package com.zoiper.zdk.android.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zoiper.zdk.Account;
import com.zoiper.zdk.Call;
import com.zoiper.zdk.Configurations.AccountConfig;
import com.zoiper.zdk.Configurations.SIPConfig;
import com.zoiper.zdk.Configurations.StunConfig;
import com.zoiper.zdk.Configurations.ZRTPConfig;
import com.zoiper.zdk.Context;
import com.zoiper.zdk.EventHandlers.AccountEventsHandler;
import com.zoiper.zdk.ExtendedError;
import com.zoiper.zdk.Providers.AccountProvider;
import com.zoiper.zdk.Result;
import com.zoiper.zdk.Types.AccountStatus;
import com.zoiper.zdk.Types.AudioVideoCodecs;
import com.zoiper.zdk.Types.LoggingLevel;
import com.zoiper.zdk.Types.OwnershipChange;
import com.zoiper.zdk.Types.ProtocolType;
import com.zoiper.zdk.Types.RPortType;
import com.zoiper.zdk.Types.RTCPFeedbackType;
import com.zoiper.zdk.Types.TransportType;
import com.zoiper.zdk.Types.Zrtp.ZRTPAuthTag;
import com.zoiper.zdk.Types.Zrtp.ZRTPCipherAlgorithm;
import com.zoiper.zdk.Types.Zrtp.ZRTPHashAlgorithm;
import com.zoiper.zdk.Types.Zrtp.ZRTPKeyAgreement;
import com.zoiper.zdk.Types.Zrtp.ZRTPSASEncoding;
import com.zoiper.zdk.android.demo.base.BaseActivity;
import com.zoiper.zdk.android.demo.call.InCallActivity;
import com.zoiper.zdk.android.demo.conference.ConferenceActivity;
import com.zoiper.zdk.android.demo.dtmf.DTMFActivity;
import com.zoiper.zdk.android.demo.incoming.IncomingCallActivity;
import com.zoiper.zdk.android.demo.messages.InMessagesActivity;
import com.zoiper.zdk.android.demo.probe.SipTransportProbe;
import com.zoiper.zdk.android.demo.util.LoggingUtils;
import com.zoiper.zdk.android.demo.video.InVideoCallActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements AccountEventsHandler {
    //Constants
    private static final String TAG = "MainActivity";

    public static final String INTENT_EXTRA_NUMBER = "number";
    public static final String INTENT_EXTRA_ACCOUNT_ID = "account_id";

    // UI
    private TextView tvNumber;
    private TextView tvStatus;
    private EditText etUsername;
    private EditText etPassword;
    private EditText etHostname;

    private Button btnRegister;
    private Button btnCreate;
    private Button btnUnregister;
    private Button btnDial;
    private Button btnMessage;
    private Button btnDialVideo;
    private Button btnIncoming;
    private Button btnConference;
    private Button btnProbe;
    private Button btnDTMF;

    private Button btnStartLog;

    // ZDK
    private Context zdkContext;
    private Account account;

    private boolean logStarted = false;

    private void printStatus(String status) {
        if (tvStatus != null) {
            tvStatus.setText(status);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setClickListeners();
    }

    @Override
    public void onZDKLoaded() {
        zdkContext = getZdkContext();
        btnCreate.setOnClickListener(v -> createAccount());
    }

    private void initViews() {
        tvNumber = findViewById(R.id.card_calls_et_number);
        tvStatus = findViewById(R.id.profile_card_tv_status);

        etUsername = findViewById(R.id.profile_card_et_username);
        etPassword = findViewById(R.id.profile_card_et_password);
        etHostname = findViewById(R.id.profile_card_et_hostname);

        btnDial = findViewById(R.id.card_calls_btn_dial);
        btnMessage = findViewById(R.id.card_calls_btn_messaging);
        btnDialVideo = findViewById(R.id.card_calls_btn_dial_video);
        btnRegister = findViewById(R.id.profile_card_btn_register);
        btnCreate = findViewById(R.id.profile_card_btn_create);
        btnUnregister = findViewById(R.id.profile_card_btn_unregister);
        btnIncoming = findViewById(R.id.profile_card_btn_incoming);
        btnConference = findViewById(R.id.profile_card_btn_conference);
        btnProbe = findViewById(R.id.profile_card_btn_probe);
        btnDTMF = findViewById(R.id.profile_card_btn_dtmf);
        btnStartLog = findViewById(R.id.profile_card_btn_log);
    }

    private void printError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    private void setClickListeners() {

        btnRegister.setOnClickListener(v -> {
            if(account != null){
                registerUser();
            }else{
                Toast.makeText(this, "Create the account first", Toast.LENGTH_SHORT).show();
            }
        });

        btnUnregister.setOnClickListener(v -> {
            if (account != null) {
                account.unRegister();
            }
        });

        btnDial.setOnClickListener(v -> startCallActivity());
        btnDialVideo.setOnClickListener(v -> startVideoCallActivity());
        btnMessage.setOnClickListener(v -> startMessageActivity());
        btnIncoming.setOnClickListener(v -> startIncomingActivity());
        btnConference.setOnClickListener(v -> startConferenceActivity());
        btnProbe.setOnClickListener(v -> startSipTransportProbe());
        btnDTMF.setOnClickListener(v -> startDTMFActivity());
        btnStartLog.setOnClickListener(v -> startLogging());
    }

    private void startLogging() {
        if (logStarted) {
            btnStartLog.setText(R.string.start_log);
            logStarted = false;
            Toast.makeText(this, "Logging stopped !", Toast.LENGTH_LONG).show();
        } else {
            String filename = LoggingUtils.generateDebugLogFilename(this);

            zdkContext.logger().logOpen(filename, "", LoggingLevel.Stack, 0);

            btnStartLog.setText(R.string.stop_log);
            logStarted = true;
            Toast.makeText(this, "Logging started !", Toast.LENGTH_LONG).show();
        }
    }

    private void startDTMFActivity() {
        if (checkNumberEntered()) {
            startActivity(DTMFActivity.class);
        }
    }

    private void startSipTransportProbe() {
        if(account != null){
            startActivity(SipTransportProbe.class);
        }else{
            Toast.makeText(this, "The account has to be created first before probe is done.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startConferenceActivity() {
        if (checkRegistration() && checkNumberEntered()) {
            startActivity(ConferenceActivity.class);
        }
    }

    private void startIncomingActivity() {
        if (checkRegistration()) {
            startActivity(IncomingCallActivity.class);
        }
    }

    private void startMessageActivity() {
        if (checkRegistration() && checkNumberEntered()) {
            startActivity(InMessagesActivity.class);
        }
    }

    /**
     * Initiate the call. First we check if the account is registered and then pass the number
     * to the In call activity.
     */
    private void startCallActivity() {
        if (account != null) {
            if (checkNumberEntered()) {
                startActivity(InCallActivity.class);
            } else {
                printError("Fill in number to dial");
            }
        } else {
            printError("Account not registered");
        }
    }

    private void startVideoCallActivity() {
        if (account != null) {
            if (checkNumberEntered()) {
                startActivity(InVideoCallActivity.class);
            } else {
                printError("Fill in number to dial");
            }
        } else {
            printError("Account not registered");
        }
    }


    /**
     * Checks if the current account status is {@link AccountStatus#Registered}.
     *
     * @return True if the current account is registered.
     */
    private boolean checkRegistration() {
        if (account != null && account.registrationStatus() == AccountStatus.Registered) {
            return true;
        } else {
            printError("Account not registered");
            return false;
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

    /**
     * Check if the user has entered a number.
     *
     * @return True if the user has entered a number.
     */
    private boolean checkNumberEntered() {
        if (!getNumberFromView().isEmpty()) {
            return true;
        } else {
            printError("Enter a number");
            return false;
        }
    }

    @NonNull
    private String getNumberFromView() {
        // Get the number.
        return tvNumber.getText().toString().trim();
    }

    /**
     * Starts an Activity by its class.
     *
     * @param activityClass
     *         Activity class (for instance MyCallActivity.class).
     */
    private void startActivity(Class<? extends Activity> activityClass) {
        String number = getNumberFromView();
        long accountID = 0;
        if (account != null) {
            accountID = account.accountID();
        }
        // Start InCallActivity
        Intent intent = new Intent(MainActivity.this, activityClass);
        // Send number and account ID.
        intent.putExtra(INTENT_EXTRA_NUMBER, number);
        intent.putExtra(INTENT_EXTRA_ACCOUNT_ID, accountID);
        MainActivity.this.startActivity(intent);
    }

    private String getEditTextValue(EditText et) {
        String result = et.getText().toString().trim();
        if (result.length() > 0) {
            return result;
        } else {
            et.setError("Required");
            return null;
        }
    }

    @SuppressWarnings("unused")
    private void registerUser() {
        if (account.registrationStatus() != AccountStatus.Registered) {
            Result createUserResult = account.createUser();
            Result registerAccountResult = account.registerAccount();

            zdkContext.accountProvider().setAsDefaultAccount(account);
        }

        printCurrentRegistrationStatus();
    }

    private void printCurrentRegistrationStatus() {
        if (account != null) {
            AccountStatus accountStatus = account.registrationStatus();

            if (accountStatus != null) {
                printStatus(accountStatus.toString());
            }
        }
    }

    private void createAccount() {
        String hostname = getEditTextValue(etHostname);
        String username = getEditTextValue(etUsername);
        String password = getEditTextValue(etPassword);

        if (hostname == null || username == null || password == null) {
            return;
        }

        if(account != null){
            Toast.makeText(this, "Account already created.", Toast.LENGTH_SHORT).show();
            return;
        }

        AccountProvider accountProvider = zdkContext.accountProvider();

        account = accountProvider.createUserAccount();

        // Set listeners on the account
        account.setStatusEventListener(this);

        // Account name - not to be confused with username
        account.accountName(username);

        // Configurations
        account.mediaCodecs(getAudioCodecs());
        account.configuration(createAccountConfig(accountProvider, hostname, username, password));

        printStatus("Created");
    }

    private AccountConfig createAccountConfig(AccountProvider ap,
                                              String hostname,
                                              String username,
                                              String password){
        final AccountConfig accountConfig = ap.createAccountConfiguration();

        accountConfig.userName(username); //@
        accountConfig.password(password); //@

        accountConfig.type(ProtocolType.SIP); //@

        accountConfig.sip(createSIPConfig(ap, hostname));

        accountConfig.reregistrationTime(60); //@

        return accountConfig;
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
        codecs.add(AudioVideoCodecs.h264); // This is for the videocall
        return codecs;
    }

    private SIPConfig createSIPConfig(AccountProvider ap, String hostname){
        final SIPConfig sipConfig = ap.createSIPConfiguration();

        sipConfig.transport(TransportType.TCP); //@

        sipConfig.domain(hostname); //@
        sipConfig.rPort(RPortType.Signaling); //@

        sipConfig.enablePrivacy(Configuration.PRIVACY);
        sipConfig.enablePreconditions(Configuration.PRECONDITIONS);
        sipConfig.enableSRTP(Configuration.SRTP); // Works only with TLS!
        sipConfig.enableVideoFMTP(Configuration.VIDEO_FMTP);

        if(Configuration.STUN){
            sipConfig.stun(createStunConfig(ap));
        }
        if(Configuration.ZRTP){
            sipConfig.zrtp(createZRTPConfig(ap));
        }

        sipConfig.rtcpFeedback(Configuration.RTCP_FEEDBACK
                ? RTCPFeedbackType.Compatibility
                : RTCPFeedbackType.Off);

        return sipConfig;
    }

    private StunConfig createStunConfig(AccountProvider ap){
        final StunConfig stunConfig = ap.createStunConfiguration();
        stunConfig.stunEnabled(true);
        stunConfig.stunServer("stun.zoiper.com");
        stunConfig.stunPort(3478);
        stunConfig.stunRefresh(30000);
        return stunConfig;
    }

    private ZRTPConfig createZRTPConfig(AccountProvider ap) {
        List<ZRTPHashAlgorithm> hashes = new ArrayList<>();
        hashes.add(ZRTPHashAlgorithm.s384);
        hashes.add(ZRTPHashAlgorithm.s256);

        List<ZRTPCipherAlgorithm> ciphers = new ArrayList<>();
        ciphers.add(ZRTPCipherAlgorithm.cipher_aes3);
        ciphers.add(ZRTPCipherAlgorithm.cipher_aes2);
        ciphers.add(ZRTPCipherAlgorithm.cipher_aes1);

        List<ZRTPAuthTag> auths = new ArrayList<>();
        auths.add(ZRTPAuthTag.hs80);
        auths.add(ZRTPAuthTag.hs32);

        List<ZRTPKeyAgreement> keyAgreements = new ArrayList<>();
        keyAgreements.add(ZRTPKeyAgreement.dh3k);
        keyAgreements.add(ZRTPKeyAgreement.dh2k);
        keyAgreements.add(ZRTPKeyAgreement.ec38);
        keyAgreements.add(ZRTPKeyAgreement.ec25);

        List<ZRTPSASEncoding> sasEncodings = new ArrayList<>();
        sasEncodings.add(ZRTPSASEncoding.sasb256);
        sasEncodings.add(ZRTPSASEncoding.sasb32);

        final ZRTPConfig zrtpConfig = ap.createZRTPConfiguration();

        zrtpConfig.enableZRTP(true);
        zrtpConfig.hash(hashes);
        zrtpConfig.cipher(ciphers);
        zrtpConfig.auth(auths);
        zrtpConfig.keyAgreement(keyAgreements);
        zrtpConfig.sasEncoding(sasEncodings);
        zrtpConfig.cacheExpiry(-1); // No expiry

        return zrtpConfig;
    }

    @Override
    public void onAccountStatusChanged(Account account, AccountStatus accountStatus, int i) {
        Log.d(TAG, "onAccountStatusChanged");
        String accountStatusStr = accountStatus.toString();
        runOnUiThread(() -> printStatus(accountStatusStr));
    }

    @Override
    public void onAccountRetryingRegistration(Account account, int i, int i1) {
        Log.d(TAG, "onAccountRetryingRegistration");
    }

    @Override
    public void onAccountIncomingCall(Account account, Call call) {
        Log.d(TAG, "onAccountIncomingCall");
    }

    @Override
    public void onAccountChatMessageReceived(Account account, String s, String s1) {
        Log.d(TAG, "onAccountChatMessageReceived");
    }

    @Override
    public void onAccountExtendedError(Account account, ExtendedError extendedError) {
        Log.d(TAG, "onAccountExtendedError");
    }

    @Override
    public void onAccountUserSipOutboundMissing(Account account) {
        Log.d(TAG, "onAccountUserSipOutboundMissing");
    }

    @Override
    public void onAccountCallOwnershipChanged(Account account,
                                              Call call,
                                              OwnershipChange ownershipChange) {
        Log.d(TAG, "onAccountCallOwnershipChanged");
    }

    private class Configuration {

        private static final boolean PRIVACY = false;

        private static final boolean PRECONDITIONS = false;

        private static final boolean STUN = false;

        private static final boolean SRTP = false;

        private static final boolean ZRTP = false;

        private static final boolean VIDEO_FMTP = true;

        private static final boolean RTCP_FEEDBACK = true;
    }
}

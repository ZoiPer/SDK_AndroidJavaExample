package com.zoiper.zdk.android.demo.probe;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.zoiper.zdk.Account;
import com.zoiper.zdk.Configurations.AccountConfig;
import com.zoiper.zdk.Configurations.SIPConfig;
import com.zoiper.zdk.EventHandlers.SIPProbeEventsHandler;
import com.zoiper.zdk.ExtendedError;
import com.zoiper.zdk.Types.ProbeState;
import com.zoiper.zdk.Types.TransportType;
import com.zoiper.zdk.android.demo.MainActivity;
import com.zoiper.zdk.android.demo.R;
import com.zoiper.zdk.android.demo.base.BaseActivity;

public class SipTransportProbe extends BaseActivity implements SIPProbeEventsHandler {

    private Account account;

    private TextView udpTextView;
    private TextView tcpTextView;
    private TextView tlsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sip_transport_probe);
        setSupportActionBar(findViewById(R.id.toolbar));

        setupActionbar(getString(R.string.sip_transport_probe));
        setupViews();
    }

    @Override
    public void onZDKLoaded() {
        long accountId = getIntent().getLongExtra(MainActivity.INTENT_EXTRA_ACCOUNT_ID, 0);
        account = getAccount(accountId);
        if(account == null) return;
        account.setProbeEventListener(this);
    }

    private void setupViews() {
         findViewById(R.id.fab_test_probe).setOnClickListener(v -> doTransportProbe());

        udpTextView = findViewById(R.id.upd_status_textview);
        tcpTextView = findViewById(R.id.tcp_status_textview);
        tlsTextView = findViewById(R.id.tls_status_textview);
    }

    private void doTransportProbe() {
        if(account == null) return;

        AccountConfig configuration = account.configuration();
        if(configuration == null) return;

        SIPConfig sipConfig = configuration.sip();
        if(sipConfig == null) return;

        String domain = sipConfig.domain();
        String username = configuration.userName();
        String password = configuration.password();

        if(domain == null || username == null || password == null) return;

        account.probeSipTransport(domain, null, username, null, password);
    }

    private void updateProbe(ProbeState probeState, String error) {
        switch (probeState) {
            case Udp:
                updateTextView(udpTextView, false, error);
                break;
            case Tcp:
                updateTextView(tcpTextView, false, error);
                break;
            case Tls:
                updateTextView(tlsTextView, false, error);
                break;
        }
    }

    private void updateProbe(TransportType transportType) {
        switch (transportType) {
            case UDP:
                updateTextView(udpTextView, true, null);
                break;
            case TCP:
                updateTextView(tcpTextView, true, null);
                break;
            case TLS:
                updateTextView(tlsTextView, true, null);
                break;
        }
    }

    private void updateTextView(TextView textView, boolean success, String error) {
        String message;
        if (success) {
            message = "Success";
        } else {
            message = "Failed";
        }
        if (error != null) {
            message = error;
        }

        textView.setText(message);
    }

    @Override
    public void onProbeError(Account account, ProbeState probeState, ExtendedError extendedError) {
        String message = extendedError.message();
        runOnUiThread(() -> updateProbe(probeState, message));
    }

    @Override
    public void onProbeState(Account account, ProbeState probeState) {
    }

    @Override
    public void onProbeSuccess(Account account, TransportType transportType) {
        TransportType jvmManagedTransportType = TransportType.valueOf(transportType.toString());
        updateProbe(jvmManagedTransportType);
    }

    /**
     * @param extendedError Important note! Don't use this argument outside of the callback
     *                      (e.g. on another thread etc.) as it's destroyed on a native level
     *                      right after the callback returns.
     */
    @Override
    public void onProbeFailed(Account account, ExtendedError extendedError) {
        String msg = extendedError.message();
        //noinspection CodeBlock2Expr
        runOnUiThread(() -> {
            Toast.makeText(this, "Probe failed: " + msg, Toast.LENGTH_LONG).show();
        });
    }
}


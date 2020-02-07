package com.zoiper.zdk.android.demo.conference;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.zoiper.zdk.Account;
import com.zoiper.zdk.Call;
import com.zoiper.zdk.Conference;
import com.zoiper.zdk.Context;
import com.zoiper.zdk.EventHandlers.CallEventsHandler;
import com.zoiper.zdk.EventHandlers.ConferenceEventsHandler;
import com.zoiper.zdk.EventHandlers.ConferenceProviderEventsHandler;
import com.zoiper.zdk.Providers.ConferenceProvider;
import com.zoiper.zdk.android.demo.MainActivity;
import com.zoiper.zdk.android.demo.R;
import com.zoiper.zdk.android.demo.base.BaseActivity;
import com.zoiper.zdk.android.demo.util.DialogUtil;

import java.util.ArrayList;
import java.util.List;

public class ConferenceActivity extends BaseActivity implements CallEventsHandler {

    private ConferenceAdapter conferenceAdapter;
    private Account account;

    private ConferencesListener conferencesListener = new ConferencesListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conference);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupActionbar(getString(R.string.conference));
        setupViews();
    }

    @Override
    public void onZDKLoaded() {
        getZdkContext()
                .conferenceProvider()
                .addConferenceProviderListener(conferencesListener);

        long accountID = getIntent().getLongExtra(MainActivity.INTENT_EXTRA_ACCOUNT_ID, 0);
        account = getAccount(accountID);

        String number = getIntent().getStringExtra(MainActivity.INTENT_EXTRA_NUMBER);
        if(number != null){
            Call call = account.createCall(number, false, false);
            createConference(call);
        }else{
            promptCreateConference();
        }
    }

    private void setupViews() {
        setupConferenceRecycler();

        findViewById(R.id.fab_add_conference).setOnClickListener(v -> promptCreateConference());
    }

    private void setupConferenceRecycler() {
        RecyclerView recyclerViewConferences = findViewById(R.id.recycler_view_conferences);

        conferenceAdapter = new ConferenceAdapter(this::promptAddCall);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
        );

        recyclerViewConferences.setAdapter(conferenceAdapter);
        recyclerViewConferences.setLayoutManager(linearLayoutManager);
    }

    private void promptAddCall(Conference conference){
        promptCreateCall(conference::addCall);
    }

    private void promptCreateCall(OnCallCreated onCallCreated) {
        DialogUtil.promptText(
            this,
            "Enter number",
            "Call",
            "Cancel",
            "Number",
            (number) -> onCallCreated.onCallCreated(
                account.createCall(number, false, false)
            ),
            () -> {
                if (conferenceAdapter.getItemCount() == 0) delayedFinish();
            }
        );
    }

    /** Add a new conference. We also set a {@link ConferenceEventsHandler}. */
    private void promptCreateConference() {
        final Context zdkContext = getZdkContext();
        // First check if the ZDK context is running.
        if (zdkContext.contextRunning()) {
            promptCreateCall(this::createConference);
        } else {
            Toast.makeText(this, getString(R.string.context_not_running), Toast.LENGTH_LONG).show();
        }
    }

    /** @param call The conference needs to have at least one call to begin with */
    private void createConference(Call call) {
        ArrayList<Call> calls = new ArrayList<>();
        calls.add(call);
        getZdkContext().conferenceProvider().createConference(calls);
    }

    private class ConferencesListener implements ConferenceProviderEventsHandler{
        @Override
        public void onConferenceAdded(ConferenceProvider confProvider, Conference conference) {
            updateAdapter();
        }

        @Override
        public void onConferenceRemoved(ConferenceProvider confProvider, Conference conference) {
            if(getZdkContext().conferenceProvider().listConferences().size() == 0) delayedFinish();
            updateAdapter();
        }

        private void updateAdapter(){
            List<Conference> conferences = getZdkContext().conferenceProvider().listConferences();
            conferenceAdapter.setConferenceList(conferences);
        }
    }

    interface OnCallCreated{
        void onCallCreated(Call call);
    }
}

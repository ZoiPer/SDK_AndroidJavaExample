package com.zoiper.zdk.android.demo.messages;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.zoiper.zdk.Account;
import com.zoiper.zdk.Call;
import com.zoiper.zdk.EventHandlers.AccountEventsHandler;
import com.zoiper.zdk.EventHandlers.MessageEventsHandler;
import com.zoiper.zdk.ExtendedError;
import com.zoiper.zdk.Message;
import com.zoiper.zdk.Result;
import com.zoiper.zdk.Types.AccountStatus;
import com.zoiper.zdk.Types.MessageStatus;
import com.zoiper.zdk.Types.MessageType;
import com.zoiper.zdk.Types.OwnershipChange;
import com.zoiper.zdk.android.demo.MainActivity;
import com.zoiper.zdk.android.demo.R;
import com.zoiper.zdk.android.demo.base.BaseActivity;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class InMessagesActivity extends BaseActivity
        implements MessageEventsHandler, AccountEventsHandler {

    // UI
    private EditText messageEditText;
    private MessageListAdapter messageAdapter;

    // ZDK
    private Account account;

    // Params
    private String number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_messages);
        setSupportActionBar(findViewById(R.id.toolbar));

        setupViews();
    }

    @Override
    public void onZDKLoaded() {
        number = getIntent().getStringExtra(MainActivity.INTENT_EXTRA_NUMBER);
        long accountId = getIntent().getLongExtra(MainActivity.INTENT_EXTRA_ACCOUNT_ID, 0);

        account = getAccount(accountId);

        setupActionbar("Messaging: " + number);
    }

    public void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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

    private void setupViews() {
        findViewById(R.id.fab).setOnClickListener(view -> sendMessage());

        RecyclerView messageRecycler = findViewById(R.id.messaging_recycler);
        messageEditText = findViewById(R.id.edittext_chatbox);
        // Setup messaging recyclerview.

        messageAdapter = new MessageListAdapter(messageRecycler);
        //messageAdapter.setHasStableIds(true);
        messageRecycler.setAdapter(messageAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,true);
        messageRecycler.setLayoutManager(linearLayoutManager);
    }

    private void insertMessage(String messageText) {
        messageAdapter.insertMessage(messageText, getCurrentTimeUsingCalendar());
    }

    public static String getCurrentTimeUsingCalendar() {
        Date date = Calendar.getInstance().getTime();
        return DateFormat.getTimeInstance().format(date);
    }

    private void sendMessage() {
        String messageString = getMessage();
        Message message = account.createMessage(MessageType.Simple);
        message.setMessageEventListener(this);
        message.peer(number);
        message.content(messageString);
        //noinspection unused
        Result messageSentResult = message.sendMessage();
        // Insert in UI.
        insertMessage(messageString);
        // Hide the keyboard.
        hideKeyboard();
        // Clear editText
        messageEditText.setText("");
        messageEditText.clearFocus();
    }

    private void onNewMessage(String username, String messageText) {
        runOnUiThread(() -> messageAdapter.insertMessage(messageText, getCurrentTimeUsingCalendar(), username));
    }

    private String getMessage() {
        if (messageEditText != null) {
            return messageEditText.getText().toString();
        } else {
            return "";
        }
    }

    @Override
    public void onMessageStatusChanged(Message message, MessageStatus messageStatus) {

    }

    @Override
    public void onMessageExtendedError(Message message, ExtendedError extendedError) {

    }

    @Override
    public void onAccountStatusChanged(Account account, AccountStatus accountStatus, int i) {

    }

    @Override
    public void onAccountRetryingRegistration(Account account, int i, int i1) {

    }

    @Override
    public void onAccountIncomingCall(Account account, Call call) {

    }

    @Override
    public void onAccountChatMessageReceived(Account account, String s, String s1) {
        onNewMessage(s, s1);
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

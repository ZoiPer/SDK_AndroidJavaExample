package com.zoiper.zdk.android.demo.messages;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zoiper.zdk.android.demo.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * MessageListAdapter
 *
 * @since 30.1.2019 г.
 */
public class MessageListAdapter extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;

    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private static final int FIRST_POSITION = 0;

    private final RecyclerView messageRecycler;

    private List<BaseMessage> messageList;

    MessageListAdapter(RecyclerView messageRecycler) {
        this.messageList = new ArrayList<>();
        this.messageRecycler = messageRecycler;
    }

    // Inflates the appropriate layout according to the ViewType.
    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        int resource = (viewType == VIEW_TYPE_MESSAGE_SENT
                ? R.layout.messages_layout
                : R.layout.messages_incoming_layout);

        View view = LayoutInflater.from(parent.getContext()).inflate(
            resource,
            parent,
            false
        );

        return (viewType == VIEW_TYPE_MESSAGE_SENT
                ? new BaseMessageHolder(view)
                : new ReceivedMessageHolder(view));
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, int position) {
        BaseMessage message = messageList.get(position);
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((BaseMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind((ReceivedMessage) message);
                break;
        }
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        BaseMessage message = messageList.get(position);

        if (message instanceof ReceivedMessage) {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        } else {
            return VIEW_TYPE_MESSAGE_SENT;
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private void addNewItem(BaseMessage newBaseMessage) {
        messageList.add(FIRST_POSITION,newBaseMessage);
        notifyItemInserted(FIRST_POSITION);
        messageRecycler.scrollToPosition(FIRST_POSITION);
    }

    void insertMessage(String messageString, String time) {
        BaseMessage newBaseMessage = new BaseMessage(messageString, time);
        addNewItem(newBaseMessage);
    }

    void insertMessage(String messageString, String time, String username) {
        ReceivedMessage newReceivedMessage = new ReceivedMessage(messageString, time, username);
        addNewItem(newReceivedMessage);
    }

    private class BaseMessageHolder extends RecyclerView.ViewHolder {

        TextView messageText, timeText;

        BaseMessageHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.base_message_body);
            timeText = itemView.findViewById(R.id.base_message_time);
        }

        void bind(BaseMessage message) {
            messageText.setText(message.getMessage());

            timeText.setText(message.getTime());
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {

        TextView messageText, timeText, nameText;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.incoming_message_body);
            timeText = itemView.findViewById(R.id.incoming_message_time);
            nameText = itemView.findViewById(R.id.incoming_message_username);
        }

        void bind(ReceivedMessage message) {
            messageText.setText(message.getMessage());

            timeText.setText(message.getTime());

            nameText.setText(message.getUsername());

        }
    }

    private class BaseMessage {

        private final String message;

        private final String time;

        BaseMessage(String message, String time) {
            this.message = message;
            this.time = time;
        }

        public String getMessage() {
            return message;
        }

        @SuppressWarnings("WeakerAccess")
        public String getTime() {
            return time;
        }
    }

    private class ReceivedMessage extends BaseMessage {

        private final String username;

        ReceivedMessage(String message, String time, String username) {
            super(message, time);
            this.username = username;
        }

        public String getUsername() {
            return username;
        }
    }

}

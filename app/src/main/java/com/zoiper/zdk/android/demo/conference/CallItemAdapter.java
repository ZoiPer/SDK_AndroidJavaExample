package com.zoiper.zdk.android.demo.conference;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zoiper.zdk.Call;
import com.zoiper.zdk.CallStatus;
import com.zoiper.zdk.EventHandlers.CallEventsHandler;
import com.zoiper.zdk.android.demo.R;
import com.zoiper.zdk.android.demo.util.TextViewSelectionUtils;

import java.util.List;

/**
 * CallItemAdapter
 *
 * @since 4.2.2019 г.
 */
public class CallItemAdapter extends RecyclerView.Adapter<CallItemAdapter.CallHolder> {

    private List<Call> callList;

    // Different actions that the UI from this adapter can trigger.
    // They're abstracted out since they're not really this adapter's responsibility
    private final MuteCall muteCall;
    private final UnmuteCall unmuteCall;
    private final RemoveCall removeCall;

    // UI thread handler.
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    CallItemAdapter(List<Call> callsList,
                    MuteCall muteCall,
                    UnmuteCall unmuteCall,
                    RemoveCall removeCall) {
        this.callList = callsList;
        this.muteCall = muteCall;
        this.unmuteCall = unmuteCall;
        this.removeCall = removeCall;
    }

    void setCallList(List<Call> callListNew) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return callList.size();
            }

            @Override
            public int getNewListSize() {
                return callListNew.size();
            }

            @Override
            public boolean areItemsTheSame(int i, int iNew) {
                return callList.get(i) == callListNew.get(iNew);
            }

            @Override
            public boolean areContentsTheSame(int i, int iNew) {
                return callList.get(i) == callListNew.get(iNew);
            }
        });
        callList = callListNew;
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public CallHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new CallHolder(layoutInflater.inflate(
                R.layout.call_item,
                parent,
                false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull CallHolder callHolder, int position) {
        callHolder.bind(callList.get(position));
    }

    @Override
    public int getItemCount() {
        return callList.size();
    }

    private void toggleMute(Call call, View view) {
        boolean newSelectedState = !view.isSelected();
        if (newSelectedState) {
            if (muteCall != null) muteCall.muteCall(call);
        } else {
            if (unmuteCall != null) unmuteCall.unmuteCall(call);
        }
        TextViewSelectionUtils.setTextViewSelected((TextView) view, newSelectedState);
    }

    interface MuteCall {
        void muteCall(Call call);
    }

    interface UnmuteCall {
        void unmuteCall(Call call);
    }

    interface RemoveCall {
        void removeCall(Call call);
    }

    class CallHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCount, tvStatus;

        // Clickable
        TextView tvMute, tvRemove;

        CallHolder(View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.call_item_name);
            tvMute = itemView.findViewById(R.id.call_item_mute);
            tvCount = itemView.findViewById(R.id.call_item_count);
            tvStatus = itemView.findViewById(R.id.call_item_status);
            tvRemove = itemView.findViewById(R.id.call_item_remove);
        }

        void bindStatusView(Call call){
            tvStatus.setText(call.status().lineStatus().toString());
            call.setCallStatusListener(new CallEventsHandler() {
                @Override
                public void onCallStatusChanged(Call call, CallStatus status) {
                    String lineStatus = status.lineStatus().toString();
                    mainHandler.post(() -> tvStatus.setText(lineStatus));
                }
            });
        }

        @SuppressLint("SetTextI18n")
        void bind(Call call) {
            tvName.setText(call.calleeName() + " (" + call.calleeNumber() + ")");
            tvCount.setText(String.valueOf(getItemCount()));

            tvMute.setOnClickListener(view -> toggleMute(call, view));
            tvRemove.setOnClickListener(view -> {
                if (removeCall != null) removeCall.removeCall(call);
            });

            bindStatusView(call);
        }
    }
}

package com.zoiper.zdk.android.demo.conference;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zoiper.zdk.Call;
import com.zoiper.zdk.Conference;
import com.zoiper.zdk.EventHandlers.ConferenceEventsHandler;
import com.zoiper.zdk.android.demo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * ConferenceAdapter
 *
 * @since 4.2.2019 г.
 */
public class ConferenceAdapter extends RecyclerView.Adapter<ConferenceAdapter.ConferenceHolder> {

    private final PromptCreateCall promptCreateCall;
    private List<Conference> conferenceList;

    ConferenceAdapter(PromptCreateCall promptCreateCall) {
        this.conferenceList = new ArrayList<>();
        this.promptCreateCall = promptCreateCall;
    }

    void setConferenceList(List<Conference> conferenceListNew) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return conferenceList.size();
            }

            @Override
            public int getNewListSize() {
                return conferenceListNew.size();
            }

            @Override
            public boolean areItemsTheSame(int i, int iNew) {
                return conferenceList.get(i).handle() == conferenceListNew.get(iNew).handle();
            }

            @Override
            public boolean areContentsTheSame(int i, int iNew) {
                Conference cOld = conferenceList.get(i);
                Conference cNew = conferenceListNew.get(i);

                return cOld.calleeName().equals(cNew.calleeName())
                        && cOld.callsCount() == cNew.callsCount();
            }
        });
        conferenceList = conferenceListNew;
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ConferenceHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new ConferenceHolder(layoutInflater.inflate(
                R.layout.conference_item,
                parent,
                false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull ConferenceHolder viewHolder, int position) {
        viewHolder.bind(conferenceList.get(position));
    }

    @Override
    public int getItemCount() {
        return conferenceList.size();
    }

    public interface PromptCreateCall {
        void promptCreateCall(Conference conference);
    }

    class ConferenceHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvCount, tvAddCall, tvRemove;
        RecyclerView rvCalls;

        ConferenceHolder(View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.conference_item_title);
            tvCount = itemView.findViewById(R.id.conference_item_count);
            tvAddCall = itemView.findViewById(R.id.conference_item_add_call);
            tvRemove = itemView.findViewById(R.id.conference_item_remove);
            rvCalls = itemView.findViewById(R.id.conference_item_recycler);
        }

        private void setupCallRecycler(Conference conference) {
            CallItemAdapter callItemAdapter = new CallItemAdapter(
                    conference.calls(),
                    conference::muteCall,
                    conference::unmuteCall,
                    (call) -> conference.removeCall(call, true)
            );

            conference.setConferenceEventsListener(new ConferenceEventsHandler() {
                @Override
                public void onConferenceParticipantJoined(Conference conf, Call call) {
                    updateCalls(conf);
                }

                @Override
                public void onConferenceParticipantRemoved(Conference conf, Call call) {
                    updateCalls(conf);
                }

                private void updateCalls(Conference conf){
                    callItemAdapter.setCallList(conf.calls());
                }
            });

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(itemView.getContext(),
                                                                              LinearLayoutManager.VERTICAL,
                                                                              false);
            rvCalls.setAdapter(callItemAdapter);
            rvCalls.setLayoutManager(linearLayoutManager);
        }

        void bind(Conference conference) {
            tvCount.setText(String.valueOf(getItemCount()));

            tvAddCall.setOnClickListener(v -> promptCreateCall.promptCreateCall(conference));
            tvRemove.setOnClickListener(v -> conference.hangUp());

            setupCallRecycler(conference);
        }
    }
}

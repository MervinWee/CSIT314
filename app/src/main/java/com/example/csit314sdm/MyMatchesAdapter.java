package com.example.csit314sdm;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

// ADAPTER: Manages the list of matches for the MyMatchesActivity.
public class MyMatchesAdapter extends RecyclerView.Adapter<MyMatchesAdapter.MatchViewHolder> {

    private List<Match> matchList;
    private Context context;

    public MyMatchesAdapter(List<Match> matchList, Context context) {
        this.matchList = matchList;
        this.context = context;
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_match, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        Match match = matchList.get(position);
        holder.bind(match);
    }

    @Override
    public int getItemCount() {
        return matchList.size();
    }

    static class MatchViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPinName;
        private TextView tvMatchCount;
        private TextView tvLastInteraction;

        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPinName = itemView.findViewById(R.id.tvPinName);
            tvMatchCount = itemView.findViewById(R.id.tvMatchCount);
            tvLastInteraction = itemView.findViewById(R.id.tvLastInteraction);
        }

        public void bind(Match match) {
            tvPinName.setText(match.getPinName());
            tvMatchCount.setText("Completed Requests: " + match.getMatchCount());

            if (match.getLastInteraction() != null) {
                long now = System.currentTimeMillis();
                CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(match.getLastInteraction().getTime(), now, DateUtils.DAY_IN_MILLIS);
                tvLastInteraction.setText("Last Interaction: " + relativeTime);
            } else {
                tvLastInteraction.setText("Last Interaction: N/A");
            }
        }
    }
}

package com.example.csit314sdm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MyMatchesAdapter extends RecyclerView.Adapter<MyMatchesAdapter.MatchViewHolder> {

    private List<User> matches = new ArrayList<>();
    private final OnMatchClickListener onMatchClickListener;

    // 1. Add an interface for click events
    public interface OnMatchClickListener {
        void onMatchClick(User user);
    }

    public MyMatchesAdapter(OnMatchClickListener onMatchClickListener) {
        this.onMatchClickListener = onMatchClickListener;
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_match, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        User user = matches.get(position);
        holder.bind(user, onMatchClickListener);
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }

    public void setMatches(List<User> matches) {
        this.matches = matches;
        notifyDataSetChanged();
    }

    static class MatchViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMatchName;
        private final TextView tvMatchEmail;

        MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMatchName = itemView.findViewById(R.id.tvMatchName);
            tvMatchEmail = itemView.findViewById(R.id.tvMatchEmail);
        }

        // 2. Update bind to set the click listener
        void bind(final User user, final OnMatchClickListener listener) {
            tvMatchName.setText(user.getFullName());
            tvMatchEmail.setText(user.getEmail());

            itemView.setOnClickListener(v -> listener.onMatchClick(user));
        }
    }
}

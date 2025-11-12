package com.example.csit314sdm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csit314sdm.entity.User;

import java.util.ArrayList;
import java.util.List;

public class MyMatchesAdapter extends RecyclerView.Adapter<MyMatchesAdapter.MatchViewHolder> {

    private List<User> matches = new ArrayList<>();
    private final OnMatchClickListener listener;

    public interface OnMatchClickListener {
        void onMatchClick(User user);
    }

    public MyMatchesAdapter(OnMatchClickListener listener) {
        this.listener = listener;
    }

    public void setMatches(List<User> matches) {
        this.matches = matches;
        notifyDataSetChanged();
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
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }

    static class MatchViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvEmail;

        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
        }

        public void bind(final User user, final OnMatchClickListener listener) {
            tvName.setText(user.getFullName());
            tvEmail.setText(user.getEmail());
            itemView.setOnClickListener(v -> listener.onMatchClick(user));
        }
    }
}

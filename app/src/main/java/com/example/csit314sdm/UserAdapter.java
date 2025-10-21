package com.example.csit314sdm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

// ADAPTER: Binds User data to the views in the RecyclerView.
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList = new ArrayList<>();
    // --- NEW: Add a listener for click events ---
    private final OnItemClickListener listener;

    // --- NEW: Create an interface to define the click handler ---
    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    // --- MODIFIED: Update the constructor to accept the listener ---
    public UserAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // I noticed your old adapter used R.layout.item_user_card. Let's keep that.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_card, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        // --- MODIFIED: Pass the user and the listener to the ViewHolder ---
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    // This method is great for updating the list. No changes needed here.
    public void setUsers(List<User> users) {
        this.userList = users;
        notifyDataSetChanged(); // Tell the RecyclerView to refresh
    }

    // Represents a single list item view
    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUserEmail;
        private final TextView tvUserRole;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            // Assuming your item_user_card.xml has these IDs.
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
        }

        // --- MODIFIED: The bind method now handles display logic and click events ---
        public void bind(final User user, final OnItemClickListener listener) {
            // Set the primary identifier (email)
            tvUserEmail.setText(user.getEmail());

            // Set the secondary info: show full name if available, otherwise show the role.
            if (user.getFullName() != null && !user.getFullName().isEmpty()) {
                tvUserRole.setText("Name: " + user.getFullName());
            } else {
                tvUserRole.setText("Role: " + user.getUserType());
            }

            // Set the click listener on the entire item view
            itemView.setOnClickListener(v -> listener.onItemClick(user));
        }
    }
}
